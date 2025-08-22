package com.clinitalPlatform.services;

import com.clinitalPlatform.dto.NotificationDTO;
import com.clinitalPlatform.enums.NotificationType;
import com.clinitalPlatform.exception.ResourceNotFoundException;
import com.clinitalPlatform.models.*;
import com.clinitalPlatform.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository ;
    @Autowired
    private DocumentMedecinRepository documentMedecinRepository;
    @Autowired
    private MedecinRepository medecinRepository;
    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private DocumentRepository documentRepository;
    @Autowired
    private RendezvousService rendezvousService;

    public List<NotificationDTO> getNotificationsByUser(Long userId, LocalDateTime since) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found" , "By id : " + userId, "" ));

        return notificationRepository
                .findByUserAndCreatedAtAfterOrderByCreatedAtDesc(user, since)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupère les notifications d'un utilisateur
     */
    public List<NotificationDTO> getUserNotifications(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found", "id", userId.toString()));

        return notificationRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }


    /**
     * Récupère le nombre de notifications non lues pour un utilisateur
     */
    public long getUnreadCount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found", "id", userId.toString()));

        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    /**
     * Marque une notification comme lue
     */
    @Transactional
    public void markAsRead(Long notificationId, Long userId) throws Exception {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found", "id", notificationId.toString()));

        if (!notification.getUser().getId().equals(userId)) {
            throw new Exception("You are not allowed to modify this notification");
        }


        notificationRepository.markAsRead(notification.getId_notif());
    }

    /**
     * Marque toutes les notifications d'un utilisateur comme lues
     */
    @Transactional
    public void markAllAsRead(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found", "id", userId.toString()));

        notificationRepository.markAllAsRead(user);
    }

    /**
     * Supprime une notification
     */
    @Transactional
    public void deleteNotification(Long notificationId, Long userId) throws Exception {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found", "id", notificationId.toString()));

        if (!notification.getUser().getId().equals(userId)) {
            throw new Exception("You can't delete this notification");
        }


        notificationRepository.delete(notification);
    }

    /**
     * Récupère les notifications filtrées par type
     */
    public List<NotificationDTO> getNotificationsByType(Long userId, NotificationType type) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found", "id", userId.toString()));

        return notificationRepository.findByUserAndTypeOrderByCreatedAtDesc(user, type)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private Long toLongSafe(Object obj) {
        if (obj instanceof Long) return (Long) obj;
        if (obj instanceof Integer) return ((Integer) obj).longValue();
        if (obj instanceof String) return Long.parseLong((String) obj);
        return null;
    }



    public NotificationDTO convertToDTO(Notification notification) {
        Map<String, Object> data = notification.getData() != null ? new HashMap<>(notification.getData()) : new HashMap<>();

        if (notification.getType() == NotificationType.SHARED_DOCUMENT) {
            Long senderId = toLongSafe(data.get("senderId"));
            Long receiverId = toLongSafe(data.get("receiverId"));

            Medecin senderMedOpt = medecinRepository.getById(senderId);
            Medecin receiverMedOpt = medecinRepository.getById(receiverId);

            Map<String, Object> senderInfo = new HashMap<>();
            senderInfo.put("name", senderMedOpt.getNom_med() + " " + senderMedOpt.getPrenom_med());
            senderInfo.put("speciality", senderMedOpt.getSpecialite().getLibelle());

            data.put("senderInfo", senderInfo);

            Map<String, Object> receiverInfo = new HashMap<>();
            receiverInfo.put("name", receiverMedOpt.getNom_med() + " " + receiverMedOpt.getPrenom_med());
            receiverInfo.put("speciality", receiverMedOpt.getSpecialite().getLibelle());

            data.put("receiverInfo", receiverInfo);

            List<Long> documentIds = Optional.ofNullable(notification.getData().get("documentIds"))
                    .map(ids -> (List<?>) ids)
                    .map(ids -> ids.stream()
                            .map(id -> Long.valueOf(id.toString()))
                            .toList())
                    .orElse(List.of());

            List<String> docTitles = documentMedecinRepository.findAllById(documentIds).stream()
                    .map(DocumentMedecin::getTitre_doc)
                    .toList();

            data.put("documentTitles", docTitles);

        }

        if(notification.getType() == NotificationType.CONSULTE_DOCUMENT || notification.getType() == NotificationType.DOWNLOAD_DOCUMENT) {
            Long documentId = toLongSafe(data.get("documentId"));
            String docType = (String) data.get("docType");
            if(docType.equals("Medecin-docs")) {
                DocumentMedecin document = documentMedecinRepository.getById(documentId);
                data.put("documentTitles", document.getTitre_doc());
            }

            if(docType.equals("Patient-docs")) {
                Document document = documentRepository.getById(documentId);
                data.put("documentTitles", document.getTitre_doc());
            }
        }

        if(notification.getType() == NotificationType.SHARED_DOCUMENT_TO_PATIENT)  {
            Long senderId = toLongSafe(data.get("senderId"));
            Long patientId = toLongSafe(data.get("patientId"));

            List<Long> documentIds = Optional.ofNullable(notification.getData().get("documentIds"))
                    .map(ids -> (List<?>) ids)
                    .map(ids -> ids.stream()
                            .map(id -> Long.valueOf(id.toString()))
                            .toList())
                    .orElse(List.of());

            Medecin senderMedOpt = medecinRepository.getById(senderId);
            Patient patient = patientRepository.getById(patientId);

            Map<String, Object> senderInfo = new HashMap<>();
            senderInfo.put("name", senderMedOpt.getNom_med() + " " + senderMedOpt.getPrenom_med());
            senderInfo.put("speciality", senderMedOpt.getSpecialite().getLibelle());

            data.put("senderInfo", senderInfo);

            Map<String, Object> patientInfo = new HashMap<>();
            patientInfo.put("name", patient.getNom_pat() + " " + patient.getPrenom_pat());
            patientInfo.put("email", patient.getPatientEmail());
            patientInfo.put("telephone", patient.getPatientTelephone());

            data.put("patientInfo", patientInfo);

            List<String> docTitles = documentMedecinRepository.findAllById(documentIds).stream()
                    .map(DocumentMedecin::getTitre_doc)
                    .toList();

            data.put("documentTitles", docTitles);
        }

        if(notification.getType() == NotificationType.REMINDER || notification.getType() == NotificationType.ERROR ||
           notification.getType() == NotificationType.ERROR_MED) {
            Rendezvous rdv = rendezvousService.getRendezvousById(notification.getRdvId());
            if (rdv != null) {
                Map<String, Object> rdvInfo = new HashMap<>();
                rdvInfo.put("mode" , rdv.getModeConsultation());
                rdvInfo.put("motif", rdv.getMotifConsultation());
                rdvInfo.put("start" , rdv.getStart());
                rdvInfo.put("startTime", rdv.getStart().format(DateTimeFormatter.ofPattern("HH:mm")));
                rdvInfo.put("endTime", rdv.getEnd().format(DateTimeFormatter.ofPattern("HH:mm")));
                data.put("rdvInfo", rdvInfo);

                Map<String, Object> medecinInfo = new HashMap<>();
                if (rdv.getMedecin() != null) {
                    medecinInfo.put("name", rdv.getMedecin().getNom_med() + " " + rdv.getMedecin().getPrenom_med());
                    medecinInfo.put("speciality", rdv.getMedecin().getSpecialite().getLibelle());
                }

                data.put("medecinInfo", medecinInfo);

                Map<String, Object> patientInfo = new HashMap<>();
                if (rdv.getPatient() != null) {
                    patientInfo.put("name", rdv.getPatient().getNom_pat() + " " + rdv.getPatient().getPrenom_pat());
                    patientInfo.put("civilite", rdv.getPatient().getCivilite_pat());
                    patientInfo.put("dateNaissance", rdv.getPatient().getDateNaissance());
                }

                data.put("patientInfo", patientInfo);
            }
        }

        if(notification.getType() == NotificationType.SHARED_DOCS_PATIENT) {
            Long senderId = toLongSafe(data.get("senderId"));
            Long receiverId = toLongSafe(data.get("receiverId"));
            String senderType = (String) data.get("senderType");

            if(senderType.equals("medecin")) {
                Medecin senderMedOpt = medecinRepository.getById(senderId);
                Map<String, Object> senderInfo = new HashMap<>();
                senderInfo.put("name", senderMedOpt.getNom_med() + " " + senderMedOpt.getPrenom_med());
                senderInfo.put("speciality", senderMedOpt.getSpecialite().getLibelle());
                data.put("senderInfo", senderInfo);
            }

            if(senderType.equals("patient")) {
                Patient patient = patientRepository.getById(senderId);
                Map<String, Object> senderInfo = new HashMap<>();
                senderInfo.put("name", patient.getNom_pat() + " " + patient.getPrenom_pat());
                senderInfo.put("email", patient.getPatientEmail());
                senderInfo.put("civilite", patient.getCivilite_pat());
                data.put("senderInfo", senderInfo);
            }

            if (receiverId != null) {
                Medecin receiverMedOpt = medecinRepository.getById(receiverId);
                Map<String, Object> receiverInfo = new HashMap<>();
                receiverInfo.put("name", receiverMedOpt.getNom_med() + " " + receiverMedOpt.getPrenom_med());
                receiverInfo.put("speciality", receiverMedOpt.getSpecialite().getLibelle());
                data.put("receiverInfo", receiverInfo);
            }

            List<Long> documentIds = Optional.ofNullable(notification.getData().get("documentIds"))
                    .map(ids -> (List<?>) ids)
                    .map(ids -> ids.stream()
                            .map(id -> Long.valueOf(id.toString()))
                            .toList())
                    .orElse(List.of());

            List<String> docTitles = documentRepository.findAllById(documentIds).stream()
                    .map(Document::getTitre_doc)
                    .toList();

            data.put("documentTitles", docTitles);
        }

            return NotificationDTO.builder()
                .id_notif(notification.getId_notif())
                .title(notification.getTitle())
                .autor(notification.getAutor())
                .message(notification.getMessage())
                .description(notification.getDescription())
                .type(notification.getType().toString())
                .requiresAction(notification.isRequiresAction())
                .time(notification.getCreatedAt().format(DateTimeFormatter.ofPattern("HH:mm")))
                .rdvTime(notification.getRdvStart() != null ? notification.getRdvStart().format(DateTimeFormatter.ofPattern("HH:mm")) : null)
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .url(notification.getUrl())
                .data(data)
                .build();
    }
}

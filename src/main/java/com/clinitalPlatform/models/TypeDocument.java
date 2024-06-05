package com.clinitalPlatform.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "types_doc")
@Data
public class TypeDocument {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_typedoc")
	private Long typeDocId;

	@Column(name = "code_type")
	private String codeType;

	@Column(name = "type_doc")
	private String docType;

}

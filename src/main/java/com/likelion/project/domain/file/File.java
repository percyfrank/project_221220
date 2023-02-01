package com.likelion.project.domain.file;

import com.likelion.project.domain.entity.BaseEntity;
import lombok.*;
import org.hibernate.annotations.SQLDelete;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@SQLDelete(sql = "UPDATE upload_file SET deleted_at = CURRENT_TIMESTAMP where id = ?")
public class UploadFile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String uploadFileName;
    private String storeFileUrl;

    public UploadFile(String uploadFileName, String storeFileUrl) {
        this.uploadFileName = uploadFileName;
        this.storeFileUrl = storeFileUrl;
    }
}

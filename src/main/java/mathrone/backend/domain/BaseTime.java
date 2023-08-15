package mathrone.backend.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class BaseTime {



        @CreatedDate
        @Temporal(TemporalType.TIMESTAMP)
        @Column(name="created_date")
        protected Date createdDate;


        @LastModifiedDate
        @Temporal(TemporalType.TIMESTAMP)
        @Column(name="modified_date")
        protected Date lastModifiedDate;




}

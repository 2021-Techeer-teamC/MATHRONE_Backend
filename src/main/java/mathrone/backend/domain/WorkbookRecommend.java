package mathrone.backend.domain;

import com.vladmihalcea.hibernate.type.array.IntArrayType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Getter;
import org.hibernate.annotations.TypeDef;

@Getter
@Entity
@Table(name = "workbook_recommended")
@TypeDef(name = "int-array", typeClass = IntArrayType.class)
public class WorkbookRecommend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //JPA 사용시 필요)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workbook_id")
    private WorkBookInfo workbookId;

    private String intro;

}

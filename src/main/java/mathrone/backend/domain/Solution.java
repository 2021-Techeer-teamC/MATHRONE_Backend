package mathrone.backend.domain;

import com.vladmihalcea.hibernate.type.array.StringArrayType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.TypeDef;

@NoArgsConstructor
@Table(name = "solution")
@Entity
@Getter
public class Solution {

    @Id
    @Column(name = "solution_id")
    private String solutionId;

    @Column(name = "solution_img")
    private String solutionImg;

    @Column(name = "problem_id")
    private String problemId;

    private int answer;
}

package mathrone.backend.domain;

import com.sun.istack.NotNull;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "problem_try")
@IdClass(ProblemTryId.class)
@EntityListeners(AuditingEntityListener.class)
public class ProblemTry {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)  // N+1 문제 회피
    @JoinColumn(name = "user_id")
    private UserInfo user;
//    @Id
//    @Column(name = "user_id")
//    private String user;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)  // N+1 문제 회피
    @JoinColumn(name = "problem_id")
    private Problem problem;
//    @Id
//    @Column(name = "problem_id")
//    private String problemId;

    @NotNull
    private boolean iscorrect;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "answer_submitted")
    @NotNull
    private int answerSubmitted;

}

package mathrone.backend.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;


import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@NoArgsConstructor
@Getter
public class UserRank {

    private String rank;
    private String score;
    private String trycnt;


    public UserRank(String rank, String score, String trycnt) {
        this.rank = rank;
        this.score = score;
        this.trycnt = trycnt;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getTrycnt() {
        return trycnt;
    }

    public void setTrycnt(String trycnt) {
        this.trycnt = trycnt;
    }
}

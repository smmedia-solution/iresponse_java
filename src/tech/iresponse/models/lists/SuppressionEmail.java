package tech.iresponse.models.lists;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;

import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.orm.ActiveRecord;
import tech.iresponse.orm.Column;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties({"columns", "database", "schema", "table", "primary"})
public class SuppressionEmail extends ActiveRecord implements Serializable {

    @Column(name = "id", primary = true, autoincrement = true, indexed = true, type = "integer", nullable = false)
    public int id;

    @Column(name = "email_md5", indexed = true, type = "text", length = 500, nullable = false)
    public String emailMd5;

    public SuppressionEmail() throws Exception {
        setDatabase("clients");
        setSchema("suppressions");
        setTable("");
    }

    public SuppressionEmail(Object paramObject) throws Exception {
        super(paramObject);
        setDatabase("clients");
        setSchema("suppressions");
        setTable("");
        load();
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof SuppressionEmail))
            return false;
        SuppressionEmail try1 = (SuppressionEmail)paramObject;
        if (!try1.exists(this))
            return false;
        if (getId() != try1.getId())
            return false;
        String str1 = getEmailMd5();
        String str2 = try1.getEmailMd5();
        return !((str1 == null) ? (str2 != null) : !str1.equals(str2));
    }

    protected boolean exists(Object instance) {
        return instance instanceof SuppressionEmail;
    }

    @Override
    public int hashCode() {
        int n = 1;
        n = n * 59 + getId();
        String str = getEmailMd5();
        return n * 59 + ((str == null) ? 43 : str.hashCode());
    }

    public int getId() {
        return this.id;
    }

    public String getEmailMd5() {
        return this.emailMd5;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setEmailMd5(String emailMd5) {
        this.emailMd5 = emailMd5;
    }

    @Override
    public String toString() {
        return "SuppressionEmail(id=" + getId() + ", emailMd5=" + getEmailMd5() + ")";
    }
}
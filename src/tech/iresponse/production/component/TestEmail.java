package tech.iresponse.production.component;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.json.Test;

import java.io.Serializable;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TestEmail implements Serializable {

    private int componentId;
    private String email;
    private String autoReplyMailbox;
    private List placeholders;

    public int getComponentId() {
        return componentId;
    }

    public void setComponentId(int componentId) {
        this.componentId = componentId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAutoReplyMailbox() {
        return autoReplyMailbox;
    }

    public void setAutoReplyMailbox(String autoReplyMailbox) {
        this.autoReplyMailbox = autoReplyMailbox;
    }

    public List getPlaceholders() {
        return placeholders;
    }

    public void setPlaceholders(List placeholders) {
        this.placeholders = placeholders;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof TestEmail))
            return false;
        TestEmail try1 = (TestEmail)paramObject;
        if (!try1.exists(this))
            return false;
        if (getComponentId() != try1.getComponentId())
            return false;
        String str1 = getEmail();
        String str2 = try1.getEmail();
        if ((str1 == null) ? (str2 != null) : !str1.equals(str2))
            return false;
        String str3 = getAutoReplyMailbox();
        String str4 = try1.getAutoReplyMailbox();
        if ((str3 == null) ? (str4 != null) : !str3.equals(str4))
            return false;
        List list1 = getPlaceholders();
        List list2 = try1.getPlaceholders();
        return !((list1 == null) ? (list2 != null) : !list1.equals(list2));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof Test;
    }

    @Override
    public int hashCode() {
        int n = 1;
        n = n * 59 + getComponentId();
        String str1 = getEmail();
        n = n * 59 + ((str1 == null) ? 43 : str1.hashCode());
        String str2 = getAutoReplyMailbox();
        n = n * 59 + ((str2 == null) ? 43 : str2.hashCode());
        List list = getPlaceholders();
        return n * 59 + ((list == null) ? 43 : list.hashCode());
    }

    @Override
    public String toString() {
        return "TestEmail(componentId=" + getComponentId() + ", email=" + getEmail() + ", autoReplyMailbox=" + getAutoReplyMailbox() + ", placeholders=" + getPlaceholders() + ")";
        }
    }

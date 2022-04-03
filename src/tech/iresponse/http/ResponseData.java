package tech.iresponse.http;

import com.fasterxml.jackson.annotation.JsonFormat;

public class ResponseData extends Response {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private Object data;

    public ResponseData(String message, Object data, int paramInt) {
        super(message, paramInt);
        this.data = data;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof ResponseData))
            return false;
        ResponseData int1 = (ResponseData)paramObject;
        if (!int1.exists(this))
            return false;
        Object object1 = getData();
        Object object2 = int1.getData();
        return !((object1 == null) ? (object2 != null) : !object1.equals(object2));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof ResponseData;
    }

    @Override
    public int hashCode() {
        int n = 1;
        Object object = getData();
        return n * 59 + ((object == null) ? 43 : object.hashCode());
    }

    public Object getData() {
        return this.data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "ResponseData(data=" + getData() + ")";
    }
}

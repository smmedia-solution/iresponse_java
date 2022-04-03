package tech.iresponse.production.component;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Rotator implements Serializable {

    private List<Object> list = new ArrayList<>();
    private int listCount = 0;
    private int index = 0;
    private int rotateAfter = 0;
    private int counter = 1;

    public Rotator(List list,int rotation) {
        if(!list.isEmpty())
        {
            this.rotateAfter = (rotation > 0) ? rotation : 1;
            this.list = list;
            this.listCount = list.size();
            this.index = 0;
            this.counter = 1;
        }
    }

    public void reset() {
        this.index = 0;
        this.counter = 1;
    }

    public synchronized void rotate() {
        if(this.counter == this.rotateAfter) {
            this.index++;
            if(this.index == this.listCount) {
                this.index = 0;
            }
            this.counter = 0;
        }
        this.counter++;
    }

    public synchronized Object getCurrentValue() {
        return !this.list.isEmpty() ? this.list.get(this.index) : null;
    }

    public synchronized Object getCurrentThenRotate() {
        Object obj = this.getCurrentValue();
        this.rotate();
        return obj;
    }
    
    public void setCurrentValue(Object value) {
        if(!this.list.isEmpty())
        {
            this.list.set(this.index,value);
        }
    }

    public List getList() {
        return list;
    }

    public int getListCount() {
        return listCount;
    }

    public int getIndex() {
        return index;
    }

    public int getRotateAfter() {
        return rotateAfter;
    }

    public int getCounter() {
        return counter;
    }

    public void setList(List paramList) {
        this.list = paramList;
    }

    public void setListCount(int paramInt) {
        this.listCount = paramInt;
    }

    public void setIndex(int paramInt) {
        this.index = paramInt;
    }

    public void setRotateAfter(int paramInt) {
        this.rotateAfter = (paramInt > 0) ? paramInt : 1;
    }

    public void setCounter(int paramInt) {
        this.counter = paramInt;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject == this)
            return true;
        if (!(paramObject instanceof Rotator))
            return false;
        Rotator for1 = (Rotator)paramObject;
        if (!for1.exists(this))
            return false;
        List list1 = getList();
        List list2 = for1.getList();
        return ((list1 == null) ? (list2 != null) : !list1.equals(list2)) ? false : ((getListCount() != for1.getListCount()) ? false : ((getIndex() != for1.getIndex()) ? false : ((getRotateAfter() != for1.getRotateAfter()) ? false : (!(getCounter() != for1.getCounter())))));
    }

    protected boolean exists(Object paramObject) {
        return paramObject instanceof Rotator;
    }

    @Override
    public int hashCode() {
        int n = 1;
        List list = getList();
        n = n * 59 + ((list == null) ? 43 : list.hashCode());
        n = n * 59 + getListCount();
        n = n * 59 + getIndex();
        n = n * 59 + getRotateAfter();
        return n * 59 + getCounter();
    }

    @Override
    public String toString() {
        return "Rotator(list=" + getList() + ", listCount=" + getListCount() + ", index=" + getIndex() + ", rotateAfter=" + getRotateAfter() + ", counter=" + getCounter() + ")";
        }
    }
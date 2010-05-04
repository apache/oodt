package jpl.eda.xmlquery;

import java.util.Vector;
import jpl.eda.util.*;

/************************************************************************
**
** Results
**
** This will manage a list of results
**
*************************************************************************/

public class Results
{
    Vector v;

    public Results()
    {

        v = new Vector();
    }

    public synchronized void addItem(Object o)
    {
        v.addElement(o);
    }

    public synchronized void removeItem(int i)
    {
        v.removeElementAt(i);
    }

    public synchronized int getCount()
    {
        return(v.size());
    }

    public synchronized Object getItem(int i)
    {
        return((Object) v.elementAt(i));
    }
}


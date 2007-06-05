/*
 * {{{ header & license
 * Copyright (c) 2007 Wisconsin Court System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.xhtmlrenderer.layout;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.xhtmlrenderer.css.parser.CounterData;

public class CounterScope {
    private final Map _counters = new HashMap();
    
    public CounterScope() {
    }
    
    public void reset(CounterScope parentScope, List counterData) {
        for (Iterator i = counterData.iterator(); i.hasNext(); ) {
            CounterData data = (CounterData)i.next();
            CounterValue parent = parentScope == null ? null : (CounterValue)parentScope._counters.get(data.getName());
            _counters.put(data.getName(), new CounterValue(parent, data.getValue()));
        }
    }
    
    public void increment(List counterData) {
        for (Iterator i = counterData.iterator(); i.hasNext(); ) {
            CounterData data = (CounterData)i.next();
            CounterValue current = (CounterValue)_counters.get(data.getName());
            if (current == null) {
                _counters.put(data.getName(), new CounterValue(null, data.getValue()));
            } else {
                _counters.put(data.getName(), 
                        new CounterValue(current.getParent(), current.getValue() + data.getValue()));
            }
        }
    }
    
    public String value(String counterName) {
        CounterValue current = (CounterValue)_counters.get(counterName);
        if (current == null) {
            return "0";
        } else {
            return Integer.toString(current.getValue());
        }
    }
    
    public String values(String counterName, String separator) {
        CounterValue current = (CounterValue)_counters.get(counterName);
        if (current == null) {
            return "0";
        } else {
            StringBuffer result = new StringBuffer();
            while (true) {
                result.append(current.getValue());
                current = current.getParent();
                if (current == null) {
                    break;
                } else {
                    result.append(separator);
                }
            }
            
            return result.toString();
        }
    }
    
    public CounterScope copyOf() {
        CounterScope result = new CounterScope();
        result._counters.putAll(_counters);
        
        return result;
    }
}

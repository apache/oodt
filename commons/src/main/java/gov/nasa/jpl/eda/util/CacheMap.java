// This software was developed by the the Jet Propulsion Laboratory, an operating division
// of the California Institute of Technology, for the National Aeronautics and Space
// Administration, an independent agency of the United States Government.
// 
// This software is copyrighted (c) 2000 by the California Institute of Technology.  All
// rights reserved.
// 
// Redistribution and use in source and binary forms, with or without modification, is not
// permitted under any circumstance without prior written permission from the California
// Institute of Technology.
//
// THIS SOFTWARE IS PROVIDED BY THE AUTHORS AND CONTRIBUTORS ``AS IS'' AND ANY EXPRESS OR
// IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
// THE AUTHOR OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
// EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
// SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
// HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
// OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
// SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
// $Id: CacheMap.java,v 1.1.1.1 2004-02-28 13:09:16 kelly Exp $

package jpl.eda.util;

import java.util.*;

// Definitions for intended functions:
//
// DEFN size(x) = The size of an object x, usually by the size(), length(), or getLength()
//                methods defined for x.
// NFED
//
// DEFN keys(x) = Set of keys from map x.
// NFED
//
// DEFN dup(x) = Duplicate of object x.
// NFED
//
// DEFN presence(x, y) = True if (x, z) or (z, x) exists in map y for any z.
// NFED
//
// DEFN advance(x, y) = sequence y such that object x in y is moved to the front of the
//                      sequence, or y if x is not an element of x.
// NFED
//
// DEFN value(v, y) = value v of (k, v) mapping in y.
// NFED
//
// A cache-map Z is a triple (c, C, M) such that c is the capacity, C is an ordered
// sequence of n keys = {k_0, k_1, ..., k_(n-1)}, and M is a set of mappings (k, v) such
// that k is the for value v, and all k's are unique.  For all k_i in C, there exists
// (k_i, v_i) in M.  size(Z) = size(C) = size(M) <= c.  The most recently used v is at the
// head of C, and the least recently used is at the tail.  When a new (k_new, v_new) is
// added to Z, C = {k_new, k_0, ..., k_(n-1)} and if size(C) >= c, the previous k_(n-1) is
// removed from C and M; and M = M U (k_new, v_new).


/** A cache map is a map with a built-in cache.
 *
 * It operates in all ways like a regular {@link java.util.Map}, except that you can store
 * only a limited number of entries.  After adding an entry that exceeds the size of the
 * cache, the map ejects the least recently used entry.
 *
 * @author Kelly
 */
public class CacheMap implements Map {
	/** Create a cache map with a default capacity of 5 entries.
	 */
	public CacheMap() {
		// FXN: [ c, C, M := 5, {}, {} ]
		this(DEFAULT_CAPACITY);
	}

	/** Create a cache map with the given capacity.
	 *
	 * @param capacity How big the cache is.
	 */
	public CacheMap(int capacity) {
		// FXN: [ c, C, M := capacity, {}, {} ]

		if (capacity < 0)
			throw new IllegalArgumentException("Can't have a negative size " + capacity + " cache map");
		this.capacity = capacity;
	}

	/** Get the cache map's capacity.
	 *
	 * @return Its capacity.
	 */
	public int getCapacity() {
		return capacity;
	}

	/** Create a cache map from the given map, having the capacity of the given map.
	 *
	 * @param map The map to copy.
	 */
	public CacheMap(Map map) {
		// FXN: [ c, C, M := size(map), keys(map), dup(map) ]

		this.capacity = map.size();
		putAll(map);
	}

	public int size() {
		// FXN: [ return value := size(M) ]
		return map.size();
	}

	public boolean isEmpty() {
		// FXN: [ return value := size(M) = 0? ]
		return map.size() == 0;
	}

	/** Returns true if there's a mapping for the specified key in the cache map.
	 *
	 * This method does not otherwise affect the cache.
	 *
	 * @param key Key to check.
	 * @return True if the cache map contains a mapping for the <var>key</var>.
	 */
	public boolean containsKey(Object key) {
		// FXN: [ return value := presence(key, M) ]
		return (map.containsKey(key));
	}

	/** Returns true if there's a mapping for the specified value in the cache map.
	 *
	 * This method does not otherwise affect the cache.
	 *
	 * @param value Value to check.
	 * @return True if the cache map contains a mapping for the <var>value</var>.
	 */
	public boolean containsValue(Object value) {
		// FXN: [ return value := presence(value, M) ]
		return map.containsValue(value);
	}

	public Object get(Object key) {
		// FXN: [ key in M -> C, return value := advance(key, C), value(key, M)
		//      | true     -> C, return value := C, null ]

		advance(key);
		return map.get(key);
	}

	public Object put(Object key, Object value) {
		// FXN: [ key in M -> C, M, return value := advance(key, C), M U (key, value), value(key, M)
		//      | size(C) < c -> C, M, return value := key || C, M U (key, value), null
		//      | true -> C, M, return value := key || {k_i | k elem C, 0 <= i <= c - 2},
		//                                      (M U (key, value)) - (k_(c-1), v), null ]

		Object old = map.put(key, value);
		if (old != null) {
			advance(key);
			return old;
		}

		cache.addFirst(key);
		if (cache.size() > capacity)
			map.remove(cache.removeLast());
		return null;
	}
	
	public Object remove(Object key) {
		// FXN: [ key in M -> C, M, return value := C - key, M - (key, v), v
		//      | true -> return value := null ]

		if (!map.containsKey(key))
			return null;
		cache.remove(key);
		return map.remove(key);
	}

	public void putAll(Map t) {
		// FXN: [ C, M := (keys(t) || C)[0..(c-1)], { (k_i, v_i) | k_i elem of (keys(t) || C)[0..(c-1)]} ]
		for (Iterator i = t.entrySet().iterator(); i.hasNext();) {
			Map.Entry entry = (Map.Entry) i.next();
			put(entry.getKey(), entry.getValue());
		}
	}

	public void clear() {
		cache.clear();
		map.clear();
	}

	public Set keySet() {
		throw new UnsupportedOperationException("Not implemented for CacheMap");
	}
	public Collection values() {
		throw new UnsupportedOperationException("Not implemented for CacheMap");
	}
	public Set entrySet() {
		throw new UnsupportedOperationException("Not implemented for CacheMap");
	}

	public boolean equals(Object rhs) {
		if (rhs == this) return true;
		if (rhs == null || !(rhs instanceof CacheMap)) return false;
		CacheMap obj = (CacheMap) rhs;
		return obj.cache.equals(cache);
	}

	public int hashCode() {
		return cache.hashCode();
	}

	/** Advance the given key to the front of the cache.
	 *
	 * If the key isn't in the cache, leave the cache alone.
	 *
	 * @param key The key to advance.
	 */
	private void advance(Object key) {
		// FXN: [ C = advance(key, C) ]

		boolean present = cache.remove(key);
		if (!present) return;
		cache.addFirst(key);
	}

	/** What the default capacity of a cache map is. */
	private static final int DEFAULT_CAPACITY = 5;

	/** The cache (C). */
	private LinkedList cache = new LinkedList();

	/** The map (M). */
	private Map map = new HashMap();

	/** The capacity of this cache map (c). */
	private int capacity;
}


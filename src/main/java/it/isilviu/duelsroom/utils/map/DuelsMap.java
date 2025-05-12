package it.isilviu.duelsroom.utils.map;

import com.sk89q.worldedit.math.BlockVector3;

import java.util.HashMap;
import java.util.List;

public class DuelsMap extends HashMap<String, List<BlockVector3>> {

    final HashMap<String, Long> duelTime = new HashMap<>();

    @Override
    public List<BlockVector3> put(String key, List<BlockVector3> value) {
        duelTime.put(key, System.currentTimeMillis());
        return super.put(key, value);
    }

    @Override
    public List<BlockVector3> remove(Object key) {
        duelTime.remove(key);
        return super.remove(key);
    }

    public long getDuelTime(String key) {
        return duelTime.getOrDefault(key, 0L);
    }
}

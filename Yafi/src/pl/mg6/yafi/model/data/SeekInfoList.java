package pl.mg6.yafi.model.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SeekInfoList implements Iterable<SeekInfo> {
	
	private final List<SeekInfo> list = new ArrayList<SeekInfo>();
	
	public void add(SeekInfo seekInfo) {
		list.add(seekInfo);
	}
	
	public SeekInfo get(int index) {
		return list.get(index);
	}
	
	public int size() {
		return list.size();
	}
	
	@Override
	public Iterator<SeekInfo> iterator() {
		return list.iterator();
	}
}

package uniba.warpingtester.model;

import java.io.File;

public class WavFile {
	private String Name;
	private long Size;
	private File file;
	
	public WavFile(String Name, long Size, File file) {
		this.Name = Name;
		this.Size = Size;
		this.file = file;
	}

	public String getName() {
		return new String(Name);
	}

	public void setName(String name) {
		Name = name;
	}

	public long getSize() {
		return Size;
	}

	public void setSize(long size) {
		Size = size;
	}
	
	@Override
	public String toString() {
		return this.Name;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}
	
}

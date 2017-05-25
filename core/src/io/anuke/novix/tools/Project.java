package io.anuke.novix.tools;

import static io.anuke.novix.Var.projectDirectory;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Disposable;

public class Project implements Disposable, Comparable<Project>{
	public long id;
	public String name;
	public int layers = 1;
	public long lastloadtime;
	
	public transient Texture cachedTexture;
	
	public Project(String name, long id){
		this.name = name;
		this.id = id;
		reloadTexture();
	}
	
	public Project(){}
	
	public void reloadTexture(){
		//TODO
		/*
		if(cachedTexture != null) cachedTexture.dispose();
		cachedTexture = new Texture(getFile());
		cachedTexture.getTextureData().prepare();
		cachedPixmap = cachedTexture.getTextureData().consumePixmap();
		*/
	}
	
	public FileHandle[] getFiles(){
		FileHandle[] files = new FileHandle[layers];
		
		for(int i = 0; i < layers; i ++){
			files[i] = projectDirectory.child(id +"-"+i+ ".png");
		}
		
		return files;
	}

	public FileHandle[] getBackupFiles(){
		FileHandle[] files = new FileHandle[layers];
		
		for(int i = 0; i < layers; i ++){
			files[i] = projectDirectory.child(id +"-"+i+ "-backup.png");
		}
		
		return files;
	}
	
	public Layer[] loadLayers(){
		Layer[] layers = new Layer[this.layers];
		for(int i = 0; i < this.layers; i ++){
			layers[i] = new Layer(new Pixmap(getFiles()[i]));
		}
		//TODO
		return layers;
	}
	
	@Override
	public int compareTo(Project other){
		if(other.lastloadtime == lastloadtime) return 0;
		return other.lastloadtime > lastloadtime ? 1 : -1;
	}
	
	@Override
	public void dispose(){
		cachedTexture.dispose();
	}
}

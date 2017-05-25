package io.anuke.novix.managers;

import static io.anuke.novix.Var.*;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectMap;

import io.anuke.novix.Novix;
import io.anuke.novix.tools.Layer;
import io.anuke.novix.tools.Project;
import io.anuke.novix.ui.DialogClasses;
import io.anuke.novix.ui.DialogClasses.InfoDialog;
import io.anuke.novix.ui.DialogClasses.NamedSizeDialog;

public class ProjectManager{
	private ObjectMap<Long, Project> projects = new ObjectMap<Long, Project>();
	private Json json = new Json();
	private Project currentProject;
	private boolean savingProject = false;
	private Array<Project> projectsort = new Array<Project>();
	private boolean backedup;

	public Iterable<Project> getProjects(){
		projectsort.clear();
		for(Project project : projects.values())
			projectsort.add(project);
		projectsort.sort();
		return projectsort;
	}

	public boolean isSavingProject(){
		return savingProject;
	}

	public Project getCurrentProject(){
		return currentProject;
	}

	public void newProject(){

		new NamedSizeDialog("New Project"){

			public void result(String name, int width, int height){
				//if(validateProjectName(name)) return;

				Project project = createNewProject(name, 1, width, height);

				openProject(project);

			}
		}.show(stage);
	}

	public Project createNewProject(String name, int layers, int width, int height){
		long id = generateProjectID();
		
		Project project = loadProject(name, id);
		project.layers = layers;
		
		FileHandle[] files = project.getFiles();
		
		for(FileHandle file : files){
			Pixmap pixmap = new Pixmap(width, height, Format.RGBA8888);
			PixmapIO.writePNG(file, pixmap);
		}

		Novix.log("Created new project with name " + name);

		return project;
	}

	public void openProject(Project project){
		core.prefs.put("lastproject", project.id);
		project.lastloadtime = System.currentTimeMillis();
		currentProject = project;

		Novix.log("Opening project \"" + project.name + "\"...");
		
		//TODO actual layer setting
		Layer[] layers = project.loadLayers();

		if(layers[0].width() > 100 || layers[0].height() > 100){
			core.prefs.put("grid", false);
		}
		
		drawing.loadLayers(layers);

		core.prefs.save();
	}

	public void copyProject(final Project project){

		new DialogClasses.InputDialog("Copy Project", project.name, "Copy Name: "){
			public void result(String text){

				try{
					long id = generateProjectID();
					
					Project newproject = new Project(text, id);
					
					int i = 0;
					for(FileHandle file : project.getFiles()){
						file.copyTo(newproject.getFiles()[i++]);
					}

					projects.put(newproject.id, newproject);
					core.updateProjects();
				}catch(Exception e){
					DialogClasses.showError(stage, "Error copying file!", e);
					e.printStackTrace();
				}
			}
		}.show(stage);
	}

	public void renameProject(final Project project){
		new DialogClasses.InputDialog("Rename Project", project.name, "Name: "){
			public void result(String text){
				project.name = text;
				core.updateProjects();
			}
		}.show(stage);
	}

	public void deleteProject(final Project project){
		if(project == currentProject){
			DialogClasses.showInfo(stage, "You cannot delete the canvas you are currently using!");
			return;
		}

		new DialogClasses.ConfirmDialog("Confirm", "Are you sure you want\nto delete this project?"){
			public void result(){
				try{
					for(FileHandle file : project.getFiles()){
						file.delete();
					}
					
					for(FileHandle file : project.getBackupFiles()){
						file.delete();
					}
					
					project.dispose();
					projects.remove(project.id);
					core.updateProjects();
				}catch(Exception e){
					DialogClasses.showError(stage, "Error deleting file!", e);
					e.printStackTrace();
				}
			}
		}.show(stage);
	}
	
	/**This is usually run asynchronously.*/
	public void saveProject(){
		saveProjectsFile();
		
		//TODO only save if each layer has been modified
		core.prefs.put("lastproject", getCurrentProject().id);
		savingProject = true;
		
		Novix.log("Starting save..");
		
		FileHandle[] files = currentProject.getFiles();
		for(int i = 0; i < files.length; i ++){
			PixmapIO.writePNG(files[i], drawing.getLayer(i).getPixmap());
		}
		
		Novix.log("Saving project.");
		savingProject = false;
	}

	private void loadProjectFile(){
		try{
			ObjectMap<String, Project> map = json.fromJson(ObjectMap.class, projectFile);
			projects = new ObjectMap<Long, Project>();
			for(String key : map.keys()){
				projects.put(Long.parseLong(key), map.get(key));
			}
		}catch(Exception e){
			e.printStackTrace();
			Novix.log("Project file nonexistant or corrupt.");
		}
	}

	private void saveProjectsFile(){
		projectFile.writeString(json.toJson(projects), false);
	}

	public void loadProjects(){
		loadProjectFile();
		
		long last = core.prefs.getLong("lastproject");

		currentProject = projects.get(last);

		for(Project project : projects.values()){
			try{
				project.reloadTexture();
			}catch(Exception e){
				e.printStackTrace();
				Novix.log("Error loading project \"" + project.name + "\": corrupt file?");
				
				//remove project, it's dead to me. don't mention it, the user doesn't need to know about this
				if(project != currentProject){
					projects.remove(project.id);
				}
			}
		}

		saveProjectsFile();
		
		if(projects.get(last) == null){ // no project selected
			Novix.log("No project selected.");
			tryLoadAnotherProject();
		}else{
			backedup = false;
			
			try{

				currentProject = projects.get(last);
				currentProject.reloadTexture();
				
				int i = 0;
				for(FileHandle file : currentProject.getFiles()){
					file.copyTo(currentProject.getBackupFiles()[i++]);
				}
				
				
				Novix.log("Loaded and backed up current project.");

			}catch(Exception e){ //corruption!
				e.printStackTrace();
				Novix.log("Project file corrupted?");
				projects.remove(currentProject.id); //remove project since it's corrupted
				//TODO backups
				/*
				//try to fix this mess
				if(getBackupFile(currentProject.id).exists()){
					try{
						getBackupFile(currentProject.id).copyTo(getFile(currentProject.id));
						currentProject.reloadTexture();
						backedup = true;
					}catch(Exception e2){ //well, we tried
						e2.printStackTrace();
						Novix.log("Backup attempt failed.");
						tryLoadAnotherProject();
					}
				//there is no backup, nowhere else to turn
				}else{
					tryLoadAnotherProject();
				}
				*/
				//show the result
				stage.addAction(Actions.sequence(Actions.delay(0.01f), new Action(){
					@Override
					public boolean act(float delta){
						new InfoDialog("Info", backedup ? "[ORANGE]Your project file has been either corrupted or deleted.\n\n[GREEN]Fortunately, a backup has been found and loaded." : "[RED]Your project file has been either corrupted or deleted.\n\n[ORANGE]A backup has not been found.\n\n[ROYAL]If you believe this is an error, try reporting the circumstances under which you last closed the app at the Google Play store listing. This could help the developer fix the problem."){

							public void result(){
								currentProject.reloadTexture();
							}
						}.show(stage);
						return true;
					}
				}));
			}
		}

	}

	void tryLoadAnotherProject(){
		if(projects.size == 0){
			currentProject = createNewProject("Untitled", 1, 16, 16);
		}else{
			currentProject = projects.values().next();
		}
		
		drawing.loadLayers(currentProject.loadLayers());
	}

	public Project loadProject(String name, long id){
		Project project = new Project(name, id);
		projects.put(project.id, project);
		return project;
	}

	public static long generateProjectID(){
		long id = MathUtils.random(Long.MAX_VALUE - 1);
		return id;
	}
}

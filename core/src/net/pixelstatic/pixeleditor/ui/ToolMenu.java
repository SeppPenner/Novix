package net.pixelstatic.pixeleditor.ui;

import static net.pixelstatic.pixeleditor.modules.Main.s;
import net.pixelstatic.gdxutils.graphics.Textures;
import net.pixelstatic.pixeleditor.graphics.PixelCanvas;
import net.pixelstatic.pixeleditor.modules.Main;
import net.pixelstatic.pixeleditor.scene2D.*;
import net.pixelstatic.pixeleditor.scene2D.DialogClasses.ClearDialog;
import net.pixelstatic.pixeleditor.scene2D.DialogClasses.ColorAlphaDialog;
import net.pixelstatic.pixeleditor.scene2D.DialogClasses.ColorizeDialog;
import net.pixelstatic.pixeleditor.scene2D.DialogClasses.ContrastDialog;
import net.pixelstatic.pixeleditor.scene2D.DialogClasses.CropDialog;
import net.pixelstatic.pixeleditor.scene2D.DialogClasses.ExportScaledDialog;
import net.pixelstatic.pixeleditor.scene2D.DialogClasses.FlipDialog;
import net.pixelstatic.pixeleditor.scene2D.DialogClasses.InvertDialog;
import net.pixelstatic.pixeleditor.scene2D.DialogClasses.ReplaceDialog;
import net.pixelstatic.pixeleditor.scene2D.DialogClasses.RotateDialog;
import net.pixelstatic.pixeleditor.scene2D.DialogClasses.ScaleDialog;
import net.pixelstatic.pixeleditor.scene2D.DialogClasses.ShiftDialog;
import net.pixelstatic.pixeleditor.scene2D.DialogClasses.SizeDialog;
import net.pixelstatic.pixeleditor.scene2D.DialogClasses.SymmetryDialog;
import net.pixelstatic.utils.MiscUtils;
import net.pixelstatic.utils.dialogs.AndroidDialogs;
import net.pixelstatic.utils.scene2D.AndroidFileChooser;
import net.pixelstatic.utils.scene2D.ColorBar;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.*;
import com.kotcrab.vis.ui.widget.VisImageButton.VisImageButtonStyle;

public class ToolMenu extends VisTable{
	private Main main;
	private VisTable menutable, optionstable;
	private VisSlider brushslider;
	private ColorBar alphabar;
	
	public ToolMenu(Main main){
		this.main = main;
		setBackground("button-window-bg");
		
		menutable = new VisTable();
		optionstable = new VisTable();
		
		top().left().add(menutable).align(Align.topLeft).expand().fill().row();
		top().left().add(optionstable).expand().fill().row();
		optionstable.row();
		
		setupMenu();
	}
	
	public void updateColor(Color color){
		alphabar.setRightColor(color);
	}
	
	public void initialize(){
		alphabar.fire(new ChangeListener.ChangeEvent()); //update alpha
		brushslider.fire(new ChangeListener.ChangeEvent());
	}
	
	private VisTextButton addMenuButton(String text){
		float height = 70f;
		VisTextButton button = new VisTextButton(text);
		menutable.top().left().add(button).width(Gdx.graphics.getWidth() / 5 - 3).height(height).expandX().fillX().padTop(5f * s).align(Align.topLeft);
		return button;
	}
	
	private void addMenu(String name, MenuButton... buttonlist){
		final ButtonMenu buttons = new ButtonMenu(name);
		buttons.getContentTable().top().left();
		for(MenuButton button : buttonlist){
			buttons.getContentTable().add(button).width(350*s).padTop(10*s).row();
		}
		//buttons.addItem("this is a test", "some description...");
		
		addMenuButton(name+ "...").addListener(new ClickListener(){
			public void clicked(InputEvent event, float x, float y){
				buttons.show(getStage());
			}
		});
	}
	
	private static class MenuButton extends VisTextButton{
		public MenuButton(String name, String desc){
			super(name);
			
			getLabel().setAlignment(Align.topLeft);
			left();
			
			row(); //this is necessary for a new row... apparently?
			add();
			row();
			
			VisLabel desclabel = new VisLabel(desc);
			desclabel.setColor(Color.GRAY);
			
			add(desclabel).align(Align.topLeft).padTop(10*s).padBottom(3*s);
			
			addListener(new ClickListener(){
				public void clicked(InputEvent event, float x, float y){
					
					MenuButton.this.clicked();
				}
			});
		}
		
		public void clicked(){
			
		}
	}
	
	private static class ButtonMenu extends VisDialog{
		public ButtonMenu(String name){
			super(name, "dialog");
			MiscUtils.addHideButton(this);
		}
	}
	
	private void setupMenu(){
		final Stage stage = main.stage;
		
		VisTextButton menu = addMenuButton("Menu");
		menu.addListener(new ClickListener(){
			public void clicked(InputEvent event, float x, float y){
				main.openProjectMenu();
			}
		});
		
		addMenu("Image",
		new MenuButton("Resize", "Change the canvas size."){
			public void clicked(){
				new SizeDialog("Resize Canvas"){
					@Override
					public void result(int width, int height){
						main.drawgrid.setCanvas(main.drawgrid.canvas.asResized(width, height));
						main.updateToolColor();
					}
				}.show(stage);
			}
		},
		new MenuButton("Crop", "Cut out a part of the image."){
			public void clicked(){
				new CropDialog().show(stage);
			}
		},
		new MenuButton("Clear", "Clear the image."){
			public void clicked(){
				new ClearDialog().show(stage);
			}
		},
		new MenuButton("Symmetry", "Configure symmetry."){
			public void clicked(){
				new SymmetryDialog().show(stage);
			}
		});
		
		addMenu("Filters", 
		new MenuButton("Colorize", "Configure image hue,\nbrightness and saturation."){
			public void clicked(){
				new ColorizeDialog().show(stage);
			}
		},
		new MenuButton("Invert", "Invert the image color."){
			public void clicked(){
				new InvertDialog().show(stage);
			}
		},
		new MenuButton("Replace", "Replace a color with\nanother color."){
			public void clicked(){
				new ReplaceDialog().show(stage);
			}
		},
		new MenuButton("Contrast", "Change image contrast."){
			public void clicked(){
				new ContrastDialog().show(stage);
			}
		},
		new MenuButton("Erase Color", "Remove a certain color\nfrom the image."){
			public void clicked(){
				new ColorAlphaDialog().show(stage);
			}
		});
		
		addMenu("Edit", 
		new MenuButton("Flip", "Flip the image."){
			public void clicked(){
				new FlipDialog().show(stage);
			}
		},
		new MenuButton("Rotate", "Rotate the image."){
			public void clicked(){
				new RotateDialog().show(stage);
			}
		},
		new MenuButton("Scale", "Scale the image."){
			public void clicked(){
				new ScaleDialog().show(stage);
			}
		},
		new MenuButton("Shift", "Move the image."){
			public void clicked(){
				new ShiftDialog().show(stage);
			}
		});
		
		addMenu("File",
		new MenuButton("Export", "Export the image as a PNG."){
			public void clicked(){
				new AndroidFileChooser(AndroidFileChooser.imageFilter, false){
					public void fileSelected(FileHandle file){
						DialogClasses.exportPixmap(main.drawgrid.canvas.pixmap, file);
					}
				}.show(stage);
			}
		},
		new MenuButton("Export Scaled", "Scale, then export the image."){
			public void clicked(){
				new ExportScaledDialog().show(stage);
			}
		},
		new MenuButton("Open", "Open an image file."){
			public void clicked(){
				new AndroidFileChooser(AndroidFileChooser.imageFilter, true){
					public void fileSelected(FileHandle file){
						try{
							main.drawgrid.setCanvas(new PixelCanvas(new Pixmap(file)));
							main.tool.onColorChange(main.selectedColor(), main.drawgrid.canvas);
						}catch(Exception e){
							e.printStackTrace();
							AndroidDialogs.showError(stage, e);
						}
					}
				}.show(stage);
			}
		});
		
		final VisLabel infolabel = new VisLabel();
	
		brushslider = new VisSlider(1, 10, 0.01f, true);
		brushslider.setValue(main.prefs.getInteger("brushsize", 1));
		final VisLabel brushlabel = new VisLabel("Brush Size: " + brushslider.getValue());

		brushslider.addListener(new ChangeListener(){
			@Override
			public void changed(ChangeEvent event, Actor actor){
				brushlabel.setText("Brush Size: " + (int)brushslider.getValue());
				main.prefs.putInteger("brushsize", (int)brushslider.getValue());
				main.drawgrid.brushSize = (int)brushslider.getValue();
			}
		});

		alphabar = new ColorBar(true);

		alphabar.setColors(Color.CLEAR.cpy(), Color.WHITE);
		alphabar.setSize(50 * s, 300 * s);
		alphabar.setSelection(main.prefs.getFloat("opacity", 1f));

		optionstable.bottom().left();

		final VisLabel opacity = new VisLabel("Opacity: " + (int)(alphabar.getSelection()*100) + "%");

		alphabar.addListener(new ChangeListener(){
			@Override
			public void changed(ChangeEvent event, Actor actor){
				opacity.setText("Opacity: " + (int)(alphabar.getSelection()*100) + "%");
				main.drawgrid.canvas.setAlpha(alphabar.getSelection());
				main.prefs.putFloat("opacity", alphabar.getSelection());
			}
		});

		final VisCheckBox grid = new VisCheckBox("Grid");

		grid.getImageStackCell().size(40 * s);

		grid.setChecked(main.prefs.getBoolean("grid", true));

		grid.addListener(new ChangeListener(){
			@Override
			public void changed(ChangeEvent event, Actor actor){
				main.drawgrid.grid = grid.isChecked();
				main.prefs.putBoolean("grid", main.drawgrid.cursormode);
				
			}
		});

		VisTextButton menubutton = new VisTextButton("Menu");
		VisTextButton settingsbutton = new VisTextButton("Settings");

		menubutton.addListener(new ClickListener(){
			public void clicked(InputEvent event, float x, float y){
				main.openProjectMenu();
			}
		});

		settingsbutton.addListener(new ClickListener(){
			public void clicked(InputEvent event, float x, float y){
				main.openSettingsMenu();
			}
		});

		final VisRadioButton cbox = new VisRadioButton("cursor mode");
		final VisRadioButton tbox = new VisRadioButton("tap mode");

		if(main.prefs.getBoolean("cursormode", true)){
			cbox.setChecked(true);
		}else{
			tbox.setChecked(true);
		}

		cbox.getImageStackCell().size(40 * s);
		tbox.getImageStackCell().size(40 * s);

		new ButtonGroup<VisRadioButton>(cbox, tbox);
		cbox.addListener(new ChangeListener(){
			@Override
			public void changed(ChangeEvent event, Actor actor){
				main.drawgrid.cursormode = cbox.isChecked();
				main.prefs.putBoolean("cursormode", main.drawgrid.cursormode);
			}
		});
		
		VisImageButtonStyle style = VisUI.getSkin().get("toggle", VisImageButtonStyle.class);
		
		VisImageButtonStyle modestyle = new VisImageButtonStyle(style);
		VisImageButtonStyle gridstyle = new VisImageButtonStyle(style);
		
		modestyle.imageUp = Textures.getDrawable("icon-cursor");
		gridstyle.imageUp = Textures.getDrawable("icon-grid");
		
		final VisImageButton modebutton = new VisImageButton(modestyle);
		modebutton.setChecked(main.prefs.getBoolean("cursormode", true));
		
		modebutton.getImageCell().size(50*s);
		
		modebutton.addListener(new ClickListener(){
			public void clicked(InputEvent event, float x, float y){
				main.prefs.putBoolean("cursormode", modebutton.isChecked());
				main.drawgrid.cursormode = modebutton.isChecked();
			}
		});
		
		final VisImageButton gridbutton = new VisImageButton(gridstyle);
		gridbutton.setChecked(main.prefs.getBoolean("grid", true));
		
		gridbutton.getImageCell().size(50*s);
		
		gridbutton.addListener(new ClickListener(){
			public void clicked(InputEvent event, float x, float y){
				main.prefs.putBoolean("grid", gridbutton.isChecked());
				main.drawgrid.grid = gridbutton.isChecked();
				
			}
		});
		
		Table menutable = new VisTable();
		Table othertable = new VisTable();

		optionstable.top().left();
		optionstable.add(menutable).growY();
		optionstable.add(othertable).grow();
		
		//TODO
		
		menutable.top().left();
		menutable.add(modebutton).size(80*s).align(Align.topLeft).padTop(8*s).row();
		menutable.add(gridbutton).size(80*s).align(Align.topLeft);
		
		othertable.bottom().right();
		
		infolabel.setAlignment(Align.topLeft, Align.left);
		
		othertable.add(brushlabel).padRight(10).minWidth(150).align(Align.center);
		othertable.add(opacity).minWidth(150).align(Align.center);
		othertable.row();
		othertable.add(brushslider).growY().padTop(20).padBottom(20).padRight(15);
		othertable.add(alphabar).padTop(20).padBottom(20);

	}

	public float getPrefWidth(){
		return Gdx.graphics.getWidth();
	}
}
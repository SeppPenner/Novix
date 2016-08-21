package net.pixelstatic.pixeleditor.scene2D;

import net.pixelstatic.gdxutils.graphics.Hue;
import net.pixelstatic.utils.MiscUtils;
import net.pixelstatic.utils.scene2D.*;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ObjectSet;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.util.ColorUtils;
import com.kotcrab.vis.ui.widget.*;
import com.kotcrab.vis.ui.widget.VisImageButton.VisImageButtonStyle;

public class ColorPicker extends VisTable{
	public static final int palettewidth = 16;
	HueBar hbar;
	ColorBar sbar;
	ColorBar vbar;
	ColorBox[] recentColors;
	ColorBox lastBox, currentBox;
	Color selectedColor;
	Color lastColor;
	VisImageButton lock;
	ObjectSet<Color> usedColors = new ObjectSet<Color>();
	boolean expandPalette;

	public ColorPicker(){
		this(true);
	}

	public ColorPicker(boolean expandPalette){
		this.expandPalette = expandPalette;
		setupUI();
	}

	public void setupUI(){
		float s = MiscUtils.densityScale();

		float width = Gdx.graphics.getWidth() - BarActor.selectionWidth * 2 - 70*s, height = 60 * s, spacing = 10 * s;

		Table table = this;

		hbar = new HueBar(){
			public void onSelectionUpdated(){
				sbar.setRightColor(getSelectedColor());
				vbar.setRightColor(getSelectedColor());
				vbar.setRightColor(sbar.getSelectedColor());
				internalColorChanged();
			}
		};
		hbar.setSize(width, height);

		sbar = new ColorBar(){
			public void onSelectionUpdated(){
				vbar.setRightColor(getSelectedColor());
				internalColorChanged();
			}
		};
		sbar.setSize(width, height);
		sbar.setColors(Color.WHITE, Color.RED);

		vbar = new ColorBar(){
			public void onSelectionUpdated(){
				sbar.brightness = selection;
				hbar.brightness = selection;
				internalColorChanged();
			}
		};
		vbar.setSize(width, height);
		vbar.setColors(Color.BLACK, Color.RED);

		table.top().center().add(hbar).padTop(spacing / 2f).row();

		table.add(sbar).padTop(spacing).row();;

		table.add(vbar).padTop(spacing);

		table.row();

		Table colordisplay = new VisTable();
		table.add(colordisplay).expand().fill().padTop(10 * s);

		lastBox = new ColorBox();
		currentBox = new ColorBox();

		lastBox.addListener(new BoxListener(lastBox));
		
		VisImageButtonStyle style = new VisImageButtonStyle(VisUI.getSkin().get("toggle", VisImageButtonStyle.class));
		style.imageChecked = VisUI.getSkin().getDrawable("icon-lock");
		style.imageUp = VisUI.getSkin().getDrawable("icon-lock-open");
		style.up = null;
		style.down = null;
		style.over = null;
		style.checked = null;
		
		lock = new VisImageButton(style);
		lock.addListener(new ChangeListener(){
			public void changed(ChangeEvent event, Actor actor){
				//lock.setColor(lock.isChecked() ? Color.RED : Color.LIGHT_GRAY);
			}
		});
		
		lock.fire(new ChangeListener.ChangeEvent());
		
		lock.getImageCell().size(50*s);

		//Image arrow = new Image(VisUI.getSkin().get("default", ColorPickerWidgetStyle.class).iconArrowRight);

		//colordisplay.add(lastBox).size(60*s);
		//colordisplay.add(arrow);
		colordisplay.add().size(70 * s);
		colordisplay.add(currentBox).size(70 * s);
		colordisplay.add(lock).size(70*s);

		table.row();

		table.add(new VisLabel("Recent Colors:")).padTop(15f * s).row();

		recentColors = new ColorBox[palettewidth];

		Table colors = new VisTable();
		table.add(colors).expand().fill().padTop(10 * s);

		float size = Gdx.graphics.getWidth() / (palettewidth / 2) - 4;

		for(int x = 0;x < palettewidth;x ++){
			final ColorBox box = new ColorBox();
			box.setDisabled(true);
			recentColors[x] = box;
			box.addListener(new BoxListener(box));

			if(x % 8 == 0) colors.row();
			colors.add(box).size(size);
		}
	}
	
	public boolean isLocked(){
		return lock.isChecked();
	}

	private class BoxListener extends InputListener{
		private ColorBox box;

		public BoxListener(ColorBox box){
			this.box = box;
		}

		public boolean touchDown(InputEvent event, float x, float y, int pointer, int button){
			box.hovered = true;
			box.setZIndex(999);
			return true;
		}

		public void touchUp(InputEvent event, float x, float y, int pointer, int button){
			setNewColor(box.getColor());
			box.hovered = false;
		}
	}

	private void internalColorChanged(){
		selectedColor = Hue.fromHSB(hbar.selection, sbar.selection, vbar.selection);
		currentBox.setColor(selectedColor);
		onColorChanged();
	}

	public void onColorChanged(){

	}

	public ColorBox[] getRecentColors(){
		return recentColors;
	}

	public void setRecentColors(Color...colors){
		for(int i = 0;i < palettewidth && i < colors.length;i ++){
			recentColors[i].setColor(colors[i]);
			recentColors[i].setDisabled(false);
		}
	}

	public void setRecentColors(ColorBox...colors){
		for(int i = 0;i < palettewidth && i < colors.length;i ++){
			if( !colors[i].isDisabled()){
				usedColors.add(colors[i].getColor());
				recentColors[i].setColor(colors[i].getColor());
				recentColors[i].setDisabled(false);
			}
		}
	}

	public Color getSelectedColor(){
		return selectedColor.cpy();
	}

	public void pushPalette(){
		addColorToPalette(selectedColor);
	}

	public void addColorToPalette(Color color){
		//prevents duped colors
		if(usedColors.contains(color)) return;
		///for(int i = 0;i < palettewidth;i ++){
		//	if(Hue.approximate(recentColors[i].getColor(), color, 0.001f) && !recentColors[i].isDisabled()) return;
		//}

		for(int i = palettewidth - 2;i >= 0;i --){
			recentColors[i + 1].setColor(recentColors[i].getColor());
			recentColors[i + 1].setDisabled(recentColors[i].isDisabled());
		}
		
		recentColors[0].setColor(color);
		usedColors.add(color);
	}

	public void setSelectedColor(Color color){
		if(selectedColor != null && expandPalette) pushPalette();
		lastColor = color.cpy();
		lastBox.setColor(color);
		setNewColor(color);
	}

	private void setNewColor(Color color){
		int[] ints = ColorUtils.RGBtoHSV(color);
		hbar.selection = ints[0] / 360f;
		sbar.selection = ints[1] / 100f;
		vbar.selection = ints[2] / 100f;
		hbar.onSelectionUpdated();
		sbar.onSelectionUpdated();
		vbar.onSelectionUpdated();
		currentBox.setColor(color);
	}
}

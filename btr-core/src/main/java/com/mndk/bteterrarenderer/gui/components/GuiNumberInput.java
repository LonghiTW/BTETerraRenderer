package com.mndk.bteterrarenderer.gui.components;

import com.mndk.bteterrarenderer.connector.Connectors;
import com.mndk.bteterrarenderer.connector.minecraft.gui.FontRendererConnector;
import com.mndk.bteterrarenderer.connector.minecraft.gui.GuiTextFieldConnector;
import com.mndk.bteterrarenderer.util.GetterSetter;
import com.mndk.bteterrarenderer.util.StringToNumber;

/**
 * Number input field class.
 */
public class GuiNumberInput {


	private final GuiTextFieldConnector parent;
	protected final GetterSetter<Double> value;
	protected final FontRendererConnector fontRenderer;
	protected final String prefix;
	protected int xPos;
	protected boolean numberValidated = true;


	public GuiNumberInput(int componentId, FontRendererConnector fontRenderer, int x, int y, int width, int height, GetterSetter<Double> value, String prefix) {
		this.parent = Connectors.SUPPLIER.newGuiTextField(componentId, fontRenderer,
				x + fontRenderer.getStringWidth(prefix) + 5, y,
				width - fontRenderer.getStringWidth(prefix) - 5, height
		);
		parent.setText(StringToNumber.formatNicely(value.get(), 3));
		parent.setMaxStringLength(50);
		this.xPos = x;
		this.value = value;
		this.fontRenderer = fontRenderer;
		this.prefix = prefix;
	}


	public void setWidth(int newWidth) {
		parent.setWidth(newWidth - fontRenderer.getStringWidth(prefix) - 5);
	}


	public void setX(int newX) {
		this.xPos = newX;
		parent.setX(newX + fontRenderer.getStringWidth(prefix) + 5);
	}


	public boolean textboxKeyTyped(char typedChar, int keyCode) {
		boolean result = parent.textboxKeyTyped(typedChar, keyCode);
		if(result) {
			String currentStr = parent.getText();
			this.numberValidated = StringToNumber.validate(currentStr);
			parent.setTextColor(numberValidated ? 0xFFFFFF : 0xFF0000);
			if(numberValidated) {
				value.set(Double.parseDouble(parent.getText()));
			}
		}
		return result;
	}


	public void drawTextBox() {
		fontRenderer.drawStringWithShadow(prefix,
				this.xPos, parent.getY() + ((parent.getHeight() - this.fontRenderer.getFontHeight()) / 2f),
				numberValidated ? 0xFFFFFF : 0xFF0000
		);
		parent.drawTextBox();
	}


	public void update() {
		parent.setText(StringToNumber.formatNicely(value.get(), 3));
	}

	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		parent.mouseClicked(mouseX, mouseY, mouseButton);
	}
}
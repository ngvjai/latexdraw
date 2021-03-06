package test.gui;

import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Spinner;
import javafx.scene.control.TitledPane;
import net.sf.latexdraw.instruments.ShapeShadowCustomiser;

import org.junit.Before;

public abstract class TestShadowStyleGUI extends TestShapePropGUI<ShapeShadowCustomiser> {
	protected CheckBox shadowCB;
	protected ColorPicker shadowColB;
	protected Spinner<Double> shadowSizeField;
	protected Spinner<Double> shadowAngleField;
	protected TitledPane mainPane;

	final protected GUIVoidCommand checkShadow = () -> clickOn(shadowCB);
	final protected GUIVoidCommand pickShadCol = () -> pickColour(shadowColB);
	final protected GUIVoidCommand incrementshadowSizeField = () -> incrementSpinner(shadowSizeField);
	final protected GUIVoidCommand incrementshadowAngleField = () -> incrementSpinner(shadowAngleField);

	@Override
	public String getFXMLPathFromLatexdraw() {
		return "view/jfx/ui/ShadowStyle.fxml";
	}

	@Override
	@Before
	public void setUp() {
		super.setUp();
		shadowCB = find("#shadowCB");
		shadowColB = find("#shadowColB");
		shadowSizeField = find("#shadowSizeField");
		shadowAngleField = find("#shadowAngleField");
		mainPane = find("#mainPane");
		ins = (ShapeShadowCustomiser)guiceFactory.call(ShapeShadowCustomiser.class);
		ins.setActivated(true);
	}
}

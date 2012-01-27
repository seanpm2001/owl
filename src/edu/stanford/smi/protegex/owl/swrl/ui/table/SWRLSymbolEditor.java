
package edu.stanford.smi.protegex.owl.swrl.ui.table;

import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.swrl.parser.SWRLParser;
import edu.stanford.smi.protegex.owl.swrl.ui.code.SWRLTextField;
import edu.stanford.smi.protegex.owl.ui.code.SymbolEditorComponent;
import edu.stanford.smi.protegex.owl.ui.code.SymbolEditorHandler;
import edu.stanford.smi.protegex.owl.ui.code.SymbolErrorDisplay;

import javax.swing.text.JTextComponent;
import java.awt.*;

public class SWRLSymbolEditor extends SymbolEditorComponent
{
	private SWRLTextField textField;

	public SWRLSymbolEditor(OWLModel model, SymbolErrorDisplay errorDisplay)
	{
		super(model, errorDisplay, false);
		textField = new SWRLTextField(model, errorDisplay);
		setLayout(new BorderLayout());
		add(textField);
	}

	public JTextComponent getTextComponent()
	{
		return textField;
	}

	protected void parseExpression() throws Exception
	{
		SWRLParser parser = new SWRLParser(getModel());
		parser.parse(textField.getText());
	}

	public void setSymbolEditorHandler(SymbolEditorHandler symbolEditorHandler)
	{
		super.setSymbolEditorHandler(symbolEditorHandler);
		textField.setSymbolEditorHandler(symbolEditorHandler);
	}
}

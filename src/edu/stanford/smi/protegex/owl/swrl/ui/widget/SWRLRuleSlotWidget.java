package edu.stanford.smi.protegex.owl.swrl.ui.widget;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Collection;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.util.LabeledComponent;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.ModalDialog;
import edu.stanford.smi.protege.widget.AbstractSlotWidget;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.swrl.model.SWRLImp;
import edu.stanford.smi.protegex.owl.swrl.model.SWRLNames;
import edu.stanford.smi.protegex.owl.swrl.parser.SWRLParseException;
import edu.stanford.smi.protegex.owl.swrl.ui.SWRLProjectPlugin;
import edu.stanford.smi.protegex.owl.swrl.ui.code.SWRLSymbolPanel;
import edu.stanford.smi.protegex.owl.swrl.ui.code.SWRLTextArea;

public class SWRLRuleSlotWidget extends AbstractSlotWidget {
	private static final String SWRL_RULE_LABEL = "SWRL Rule";
	private static final String SWRL_RULE_NOT_SAVED_LABEL = "SWRL Rule - NOT SAVED!";
	
	private SWRLTextArea swrlTextArea;
	private LabeledComponent swrlTextAreaLabeledComponent;
	
	private String ruleExpressionInKb = new String();

	private FocusListener _focusListener = new FocusAdapter() {
		public void focusLost(FocusEvent event) {
			commitChanges();
		}
	};

	public void initialize() {
		buildGUI();

		setPreferredColumns(4);
		setPreferredRows(5);
	}


	protected void buildGUI() {
		final OWLModel owlModel = (OWLModel) getKnowledgeBase();

		SWRLSymbolPanel symbolPanel = new SWRLSymbolPanel(owlModel, false, false) {
			@Override
			public void setErrorFlag(boolean error) {
				if (error) {
					setInvalidValueBorder();
				}
				super.setErrorFlag(error);
			}
		};
				
		
		swrlTextArea = new SWRLTextArea(owlModel, symbolPanel) {
		    protected void updateErrorDisplay() {
		        String uniCodeText = getText();
		        swrlTextAreaLabeledComponent.setHeaderLabel(ruleExpressionInKb.equals(getText()) ? 
		        		SWRL_RULE_LABEL : SWRL_RULE_NOT_SAVED_LABEL);
		        try {
		            checkUniCodeExpression(uniCodeText);
		            getErrorSymbolDisplay().displayError((Throwable) null);
		            setBackground(Color.white);
		            setNormalBorder();
		        }
		        catch (Throwable ex) {
		            getErrorSymbolDisplay().setErrorFlag(true);
		            getErrorSymbolDisplay().displayError(ex.getMessage());
		            setBackground(new Color(240, 240, 240));
		            setInvalidValueBorder();
		        }
		    }
		};

		swrlTextArea.addFocusListener(_focusListener);
		//swrlTextArea.addKeyListener(_keyListener);

		symbolPanel.setSymbolEditor(swrlTextArea);

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(new JScrollPane(swrlTextArea), BorderLayout.CENTER);
		panel.add(symbolPanel, BorderLayout.SOUTH);

		swrlTextAreaLabeledComponent = new LabeledComponent(SWRL_RULE_LABEL, panel, true);

		add(swrlTextAreaLabeledComponent);
	}

	@Override
	public void setInstance(Instance newInstance) {
		
		commitChanges();
		
		updateGUI((SWRLImp) newInstance);

		super.setInstance(newInstance);
	}


	protected void updateGUI(SWRLImp imp) {
		swrlTextAreaLabeledComponent.setHeaderLabel(SWRL_RULE_LABEL);
		
		ruleExpressionInKb = imp == null ? "" : imp.getBrowserText();		
		swrlTextArea.setText(ruleExpressionInKb);
		
		//do we want this?
		swrlTextArea.reformatText();
		ruleExpressionInKb = swrlTextArea.getText();
	}


	public boolean commitChanges() {
		SWRLImp swrlimp = (SWRLImp) getInstance();

		if (swrlimp == null) {
			return true;
		}

		try {
			swrlimp.setExpression(swrlTextArea.getText());			
			ruleExpressionInKb = swrlTextArea.getText();
			
			swrlTextAreaLabeledComponent.setHeaderLabel(SWRL_RULE_LABEL);
		} catch (SWRLParseException e) {
			setInvalidValueBorder();
			swrlTextArea.getErrorSymbolDisplay().displayError(e);
			
			swrlTextAreaLabeledComponent.setHeaderLabel(SWRL_RULE_NOT_SAVED_LABEL);			
			return false;
		}
		return true;
	}	


	public static boolean isSuitable(Cls cls, Slot slot, Facet facet) {
		KnowledgeBase kb = cls.getKnowledgeBase();

		if (!(kb instanceof OWLModel)) {
			return false;
		}

		if (!SWRLProjectPlugin.isSWRLImported((OWLModel)kb)) {
			return false;
		}

		if (slot.getName().equals(SWRLNames.Slot.BODY)) {
			return true;
		}

		return false;
	}
	
	public String getSwrlTextAreaText() {
		return swrlTextArea.getText();
	}

}

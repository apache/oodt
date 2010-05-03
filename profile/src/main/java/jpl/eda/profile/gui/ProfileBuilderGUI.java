package jpl.eda.profile.gui;

import javax.swing.JSeparator;
import javax.swing.JMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.BorderLayout;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.JFileChooser;

import jpl.eda.profile.Profile;
import jpl.eda.profile.ProfileElement;
import jpl.eda.profile.RangedProfileElement;
import jpl.eda.profile.gui.profileTree;

import jpl.eda.profile.gui.pstructs.ProfilePrinter;


import java.util.Iterator;
import java.util.Enumeration;
import java.util.List;


import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JButton;
import org.xml.sax.SAXException;

/**
* This code was generated using CloudGarden's Jigloo
* SWT/Swing GUI Builder, which is free for non-commercial
* use. If Jigloo is being used commercially (ie, by a
* for-profit company or business) then you should purchase
* a license - please visit www.cloudgarden.com for details.
*/
public class ProfileBuilderGUI extends javax.swing.JFrame {
	private JButton jButton1;
	private JEditorPane jEditorPane1;
	private JScrollPane jPanel3;
	private profileTree jTree1;
	private JScrollPane jPanel2;
	private JPanel jPanel1;
	private JMenuItem helpMenuItem;
	private JMenu jMenu5;
	private JMenuItem deleteMenuItem;
	private JSeparator jSeparator1;
	private JMenuItem pasteMenuItem;
	private JMenuItem copyMenuItem;
	private JMenuItem cutMenuItem;
	private JMenu jMenu4;
	private JMenuItem exitMenuItem;
	private JSeparator jSeparator2;
	private JMenuItem closeFileMenuItem;
	private JMenuItem saveMenuItem;
	private JMenuItem openFileMenuItem;
	private JMenuItem newFileMenuItem;
	private JMenu jMenu3;
	private JMenuBar jMenuBar1;
	
	private Profile createdProfile=null;
	

	public ProfileBuilderGUI() {
		initGUI();
	}

	/**
	* Initializes the GUI.
	* Auto-generated code - any changes you make will disappear.
	*/
	public void initGUI(){
		try {
			preInitGUI();
	
			try {
				javax.swing.UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
			} catch(Exception e) {
				e.printStackTrace();
			}
			jPanel1 = new JPanel();
			jPanel2 = new JScrollPane();
			jTree1 = new profileTree();
			jPanel3 = new JScrollPane();
			jEditorPane1 = new JEditorPane();
			jButton1 = new JButton();
	
			BorderLayout thisLayout = new BorderLayout();
			this.getContentPane().setLayout(thisLayout);
			thisLayout.setHgap(0);
			thisLayout.setVgap(0);
			this.setTitle("OODT Profile Builder");
			this.setSize(new java.awt.Dimension(682,387));
	
			BorderLayout jPanel1Layout = new BorderLayout();
			jPanel1.setLayout(jPanel1Layout);
			jPanel1Layout.setHgap(0);
			jPanel1Layout.setVgap(0);
			this.getContentPane().add(jPanel1, BorderLayout.CENTER);
	
			jPanel2.setPreferredSize(new java.awt.Dimension(231,339));
			jPanel2.setAutoscrolls(true);
			jPanel1.add(jPanel2, BorderLayout.WEST);
	
			jTree1.setEditable(true);
			jTree1.setPreferredSize(new java.awt.Dimension(426,800));
			jTree1.setAutoscrolls(true);
			jTree1.setMaximumSize(new java.awt.Dimension(426,800));
			jTree1.setOpaque(true);
			jTree1.setSize(new java.awt.Dimension(426,800));
			jPanel2.add(jTree1);
			jPanel2.setViewportView(jTree1);
	
			jPanel3.setPreferredSize(new java.awt.Dimension(444,339));
			jPanel3.setAutoscrolls(true);
			jPanel1.add(jPanel3, BorderLayout.EAST);
	
			jEditorPane1.setEditable(false);
			jEditorPane1.setPreferredSize(new java.awt.Dimension(414,320));
			jEditorPane1.setAutoscrolls(true);
			jPanel3.add(jEditorPane1);
			jPanel3.setViewportView(jEditorPane1);
	
			jButton1.setText("Show XML");
			this.getContentPane().add(jButton1, BorderLayout.SOUTH);
			jButton1.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					jButton1ActionPerformed(evt);
				}
			});
			jMenuBar1 = new JMenuBar();
			jMenu3 = new JMenu();
			newFileMenuItem = new JMenuItem();
			openFileMenuItem = new JMenuItem();
			saveMenuItem = new JMenuItem();
			closeFileMenuItem = new JMenuItem();
			jSeparator2 = new JSeparator();
			exitMenuItem = new JMenuItem();
			jMenu4 = new JMenu();
			cutMenuItem = new JMenuItem();
			copyMenuItem = new JMenuItem();
			pasteMenuItem = new JMenuItem();
			jSeparator1 = new JSeparator();
			deleteMenuItem = new JMenuItem();
			jMenu5 = new JMenu();
			helpMenuItem = new JMenuItem();
	
			setJMenuBar(jMenuBar1);
	
			jMenu3.setText("File");
			jMenu3.setVisible(true);
			jMenuBar1.add(jMenu3);
	
			newFileMenuItem.setText("New Profile");
			newFileMenuItem.setVisible(true);
			newFileMenuItem.setPreferredSize(new java.awt.Dimension(28,16));
			newFileMenuItem.setBounds(new java.awt.Rectangle(5,5,28,16));
			jMenu3.add(newFileMenuItem);
			newFileMenuItem.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					newFileMenuItemActionPerformed(evt);
				}
			});
	
			openFileMenuItem.setText("Open Profile");
			openFileMenuItem.setVisible(true);
			openFileMenuItem.setBounds(new java.awt.Rectangle(5,5,60,30));
			jMenu3.add(openFileMenuItem);
			openFileMenuItem.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					openFileMenuItemActionPerformed(evt);
				}
			});
	
			saveMenuItem.setText("Save Profile");
			saveMenuItem.setVisible(true);
			saveMenuItem.setVerifyInputWhenFocusTarget(false);
			saveMenuItem.setBounds(new java.awt.Rectangle(5,5,60,30));
			jMenu3.add(saveMenuItem);
			saveMenuItem.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					saveMenuItemActionPerformed(evt);
				}
			});
	
			closeFileMenuItem.setText("Close");
			closeFileMenuItem.setVisible(true);
			closeFileMenuItem.setBounds(new java.awt.Rectangle(5,5,60,30));
			jMenu3.add(closeFileMenuItem);
	
			jSeparator2.setVisible(true);
			jSeparator2.setBounds(new java.awt.Rectangle(5,5,60,30));
			jMenu3.add(jSeparator2);
	
			exitMenuItem.setText("Exit");
			exitMenuItem.setVisible(true);
			exitMenuItem.setBounds(new java.awt.Rectangle(5,5,60,30));
			jMenu3.add(exitMenuItem);
			exitMenuItem.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					exitMenuItemActionPerformed(evt);
				}
			});
	
			jMenu4.setText("Edit");
			jMenu4.setVisible(true);
			jMenuBar1.add(jMenu4);
	
			cutMenuItem.setText("Cut");
			cutMenuItem.setVisible(true);
			cutMenuItem.setPreferredSize(new java.awt.Dimension(27,16));
			cutMenuItem.setBounds(new java.awt.Rectangle(5,5,27,16));
			jMenu4.add(cutMenuItem);
	
			copyMenuItem.setText("Copy");
			copyMenuItem.setVisible(true);
			copyMenuItem.setBounds(new java.awt.Rectangle(5,5,60,30));
			jMenu4.add(copyMenuItem);
	
			pasteMenuItem.setText("Paste");
			pasteMenuItem.setVisible(true);
			pasteMenuItem.setBounds(new java.awt.Rectangle(5,5,60,30));
			jMenu4.add(pasteMenuItem);
	
			jSeparator1.setVisible(true);
			jSeparator1.setBounds(new java.awt.Rectangle(5,5,60,30));
			jMenu4.add(jSeparator1);
	
			deleteMenuItem.setText("Delete");
			deleteMenuItem.setVisible(true);
			deleteMenuItem.setBounds(new java.awt.Rectangle(5,5,60,30));
			jMenu4.add(deleteMenuItem);
	
			jMenu5.setText("Help");
			jMenu5.setVisible(true);
			jMenuBar1.add(jMenu5);
	
			helpMenuItem.setText("Help");
			helpMenuItem.setVisible(true);
			helpMenuItem.setPreferredSize(new java.awt.Dimension(31,16));
			helpMenuItem.setBounds(new java.awt.Rectangle(5,5,31,16));
			jMenu5.add(helpMenuItem);
	
			postInitGUI();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/** Add your pre-init code in here 	*/
	public void preInitGUI(){
	}

	/** Add your post-init code in here 	*/
	public void postInitGUI(){
	}

	/** Auto-generated main method */
	public static void main(String[] args){
		//showGUI();
		showMainGUI();
	}

	public static void showMainGUI(){
		try {
			ProfileBuilderGUI inst = new ProfileBuilderGUI();
			inst.setVisible(true);
			inst.getJTree1().addMouseListener(new LeafListener(inst.getJTree1()));
		    inst.getJTree1().setExpandsSelectedPaths(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}



	
	
	
	/**
	* This static method creates a new instance of this class and shows
	* it inside a new JFrame, (unless it is already a JFrame).
	*
	* It is a convenience method for showing the GUI, but it can be
	* copied and used as a basis for your own code.	*
	* It is auto-generated code - the body of this method will be
	* re-generated after any changes are made to the GUI.
	* However, if you delete this method it will not be re-created.	*/
	public static void showGUI(){
		try {
			ProfileBuilderGUI inst = new ProfileBuilderGUI();
			inst.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
/**
* Auto-generated method
*/public JPanel getJPanel1(){
return jPanel1;	}
/**
* Auto-generated method
*/public JScrollPane getJPanel2(){
return jPanel2;	}
/**
* Auto-generated method
*/public profileTree getJTree1(){
return jTree1;	}

public void setJTree1(profileTree jt){
	jTree1 = jt;
	repaint();
}
/**
* Auto-generated method
*/public JScrollPane getJPanel3(){
return jPanel3;	}
/**
* Auto-generated method
*/public JEditorPane getJEditorPane1(){
return jEditorPane1;	}



	/** Auto-generated event handler method */
	protected void newFileMenuItemActionPerformed(ActionEvent evt){
		//TODO add your handler code here
		
			createdProfile = new Profile();
			jEditorPane1.setText(new ProfilePrinter(createdProfile,"http://oodt.jpl.nasa.gov/dtd/prof.dtd").toXMLString());
			
			getJTree1().setModel(generateModelFromProfile(createdProfile));
			//getJTree1().addMouseListener(new LeafListener(getJTree1()));
	}

	/** Auto-generated event handler method */
	protected void exitMenuItemActionPerformed(ActionEvent evt){
		//TODO add your handler code here
		System.exit(1);
	}
	
	private void addElementsFromTreeNode(TreeNode t, List multiValueElem){
		for(Enumeration i = t.children(); i.hasMoreElements(); ){
            DefaultMutableTreeNode theNode = (DefaultMutableTreeNode)i.nextElement();
            
            String theStr = (String)theNode.getUserObject();
            multiValueElem.add(theStr);    
		}
	}
	
	
	
	private Profile generateProfileFromModel(TreeModel dtm){
		
		Profile p = new Profile();
		DefaultMutableTreeNode theProfileRoot = (DefaultMutableTreeNode)dtm.getRoot();
		
		//get the profile attributes
		//children List
		//id String
		//parent String
		//regAuthority String
		//revisionNotes List
		//securityType String
		//statusID String
		//type String
		//version String
		TreeNode theProfAttrRoot = theProfileRoot.getChildAt(0);

		TreeNode theProfAttr_Children = theProfAttrRoot.getChildAt(0);
		addElementsFromTreeNode(theProfAttr_Children,p.getProfileAttributes().getChildren());

		TreeNode theProfAttr_IDRoot = theProfAttrRoot.getChildAt(1);
		DefaultMutableTreeNode theProfAttr_ID = (DefaultMutableTreeNode)theProfAttr_IDRoot.getChildAt(0);
		p.getProfileAttributes().setID((String)theProfAttr_ID.getUserObject());
		
		TreeNode theProfAttr_ParentRoot = theProfAttrRoot.getChildAt(2);
		DefaultMutableTreeNode theProfAttr_Parent = (DefaultMutableTreeNode)theProfAttr_ParentRoot.getChildAt(0);
		p.getProfileAttributes().setParent((String)theProfAttr_Parent.getUserObject());
		
		
		TreeNode theProfAttr_RegAuthorityRoot = theProfAttrRoot.getChildAt(3);
		DefaultMutableTreeNode theProfAttr_RegAuthority = (DefaultMutableTreeNode)theProfAttr_RegAuthorityRoot.getChildAt(0);
		p.getProfileAttributes().setRegAuthority((String)theProfAttr_RegAuthority.getUserObject());
		
		TreeNode theProfAttr_revNotes = theProfAttrRoot.getChildAt(4);
		addElementsFromTreeNode(theProfAttr_revNotes,p.getProfileAttributes().getRevisionNotes());

		TreeNode theProfAttr_SecurityTypeRoot = theProfAttrRoot.getChildAt(5);
		DefaultMutableTreeNode theProfAttr_SecurityType = (DefaultMutableTreeNode)theProfAttr_SecurityTypeRoot.getChildAt(0);
		p.getProfileAttributes().setSecurityType((String)theProfAttr_SecurityType.getUserObject());
		
		TreeNode theProfAttr_StatusIDRoot = theProfAttrRoot.getChildAt(6);
		DefaultMutableTreeNode theProfAttr_StatusID = (DefaultMutableTreeNode)theProfAttr_StatusIDRoot.getChildAt(0);
		p.getProfileAttributes().setStatusID((String)theProfAttr_StatusID.getUserObject());
		
		TreeNode theProfAttr_TypeRoot = theProfAttrRoot.getChildAt(7);
		DefaultMutableTreeNode theProfAttr_Type = (DefaultMutableTreeNode)theProfAttr_TypeRoot.getChildAt(0);
		p.getProfileAttributes().setType((String)theProfAttr_Type.getUserObject());
		
		TreeNode theProfAttr_VersionRoot = theProfAttrRoot.getChildAt(8);
		DefaultMutableTreeNode theProfAttr_Version = (DefaultMutableTreeNode)theProfAttr_VersionRoot.getChildAt(0);
		p.getProfileAttributes().setVersion((String)theProfAttr_Version.getUserObject());
				

		//resource attributes
		//aggregation - string
		//class - string
		//contexts - list
		//contributors - list
		//coverages - list
		//creators - list
		//dates - list
		//description - string
		//formats - list
		//identifier - string
		//languages - list
		//locations - list
		//publishers - list
		//relations - list
		//rights - list
		//sources - list
		//subjects - list
		//title - string
		//types - list
		
		TreeNode theResAttrRoot = theProfileRoot.getChildAt(1);
		
		TreeNode ra_aggRoot=theResAttrRoot.getChildAt(0);
		TreeNode ra_classRoot=theResAttrRoot.getChildAt(1);
		TreeNode ra_contextRoot=theResAttrRoot.getChildAt(2);
		TreeNode ra_contribRoot=theResAttrRoot.getChildAt(3);
		TreeNode ra_coverageRoot=theResAttrRoot.getChildAt(4);
		TreeNode ra_creatorRoot=theResAttrRoot.getChildAt(5);
		TreeNode ra_datesRoot=theResAttrRoot.getChildAt(6);
		TreeNode ra_descRoot=theResAttrRoot.getChildAt(7);
		TreeNode ra_formatsRoot=theResAttrRoot.getChildAt(8);
		TreeNode ra_identifierRoot=theResAttrRoot.getChildAt(9);
		TreeNode ra_langRoot=theResAttrRoot.getChildAt(10);
		TreeNode ra_locationRoot=theResAttrRoot.getChildAt(11);
		TreeNode ra_publishersRoot=theResAttrRoot.getChildAt(12);
		TreeNode ra_relationsRoot=theResAttrRoot.getChildAt(13);
		TreeNode ra_rightsRoot=theResAttrRoot.getChildAt(14);
		TreeNode ra_sourcesRoot=theResAttrRoot.getChildAt(15);
		TreeNode ra_subjectsRoot=theResAttrRoot.getChildAt(16);
		TreeNode ra_titleRoot=theResAttrRoot.getChildAt(17);
		TreeNode ra_typesRoot=theResAttrRoot.getChildAt(18);
		
		DefaultMutableTreeNode ra_agg = (DefaultMutableTreeNode)ra_aggRoot.getChildAt(0);
		DefaultMutableTreeNode ra_class = (DefaultMutableTreeNode)ra_classRoot.getChildAt(0);
		
		
		p.getResourceAttributes().setResAggregation((String)ra_agg.getUserObject());
		p.getResourceAttributes().setResClass((String)ra_class.getUserObject());
		addElementsFromTreeNode(ra_contextRoot,p.getResourceAttributes().getResContexts());
		addElementsFromTreeNode(ra_contribRoot,p.getResourceAttributes().getContributors());
		addElementsFromTreeNode(ra_coverageRoot,p.getResourceAttributes().getCoverages());
		addElementsFromTreeNode(ra_creatorRoot,p.getResourceAttributes().getCreators());
		addElementsFromTreeNode(ra_datesRoot,p.getResourceAttributes().getDates());
		
		DefaultMutableTreeNode ra_desc = (DefaultMutableTreeNode)ra_descRoot.getChildAt(0);
		DefaultMutableTreeNode ra_identifier = (DefaultMutableTreeNode)ra_identifierRoot.getChildAt(0);
		
		p.getResourceAttributes().setDescription((String)ra_desc.getUserObject());
		p.getResourceAttributes().setIdentifier((String)ra_identifier.getUserObject());
		
		addElementsFromTreeNode(ra_formatsRoot,p.getResourceAttributes().getFormats());
		addElementsFromTreeNode(ra_langRoot,p.getResourceAttributes().getLanguages());
		addElementsFromTreeNode(ra_locationRoot,p.getResourceAttributes().getResLocations());
		addElementsFromTreeNode(ra_publishersRoot,p.getResourceAttributes().getPublishers());
		addElementsFromTreeNode(ra_relationsRoot,p.getResourceAttributes().getRelations());
		addElementsFromTreeNode(ra_rightsRoot,p.getResourceAttributes().getRights());
		addElementsFromTreeNode(ra_sourcesRoot,p.getResourceAttributes().getSources());
		addElementsFromTreeNode(ra_subjectsRoot,p.getResourceAttributes().getSubjects());
		addElementsFromTreeNode(ra_typesRoot,p.getResourceAttributes().getTypes());
		
		DefaultMutableTreeNode ra_title = (DefaultMutableTreeNode)ra_titleRoot.getChildAt(0);
		p.getResourceAttributes().setTitle((String)ra_title.getUserObject());
		
		//handle profile elements here
		TreeNode theProfElemRoot = theProfileRoot.getChildAt(2);
		
		for(Enumeration e = theProfElemRoot.children(); e.hasMoreElements(); ){
			DefaultMutableTreeNode profElemN_Root = (DefaultMutableTreeNode)e.nextElement();
			System.out.println("Got Profile Element "+(String)profElemN_Root.getUserObject());
			ProfileElement profElem = makeProfileElementFromTreeNode(p,profElemN_Root);
			
			if(profElem != null){
				System.out.println("Making profile element");
				System.out.println(profElem.toString());
				
				p.getProfileElements().put((String)profElemN_Root.getUserObject(),profElem);
			}
			
			
		}
		
		
		return p;
	}
	
	private ProfileElement makeProfileElementFromTreeNode(Profile theProfile,DefaultMutableTreeNode tn){
		
		ProfileElement theProfileElement = new RangedProfileElement(theProfile);
		
		DefaultMutableTreeNode commentRoot = (DefaultMutableTreeNode)tn.getChildAt(0);
		DefaultMutableTreeNode descRoot = (DefaultMutableTreeNode)tn.getChildAt(1);
		DefaultMutableTreeNode idRoot = (DefaultMutableTreeNode)tn.getChildAt(2);
		DefaultMutableTreeNode moRoot = (DefaultMutableTreeNode)tn.getChildAt(3);
		DefaultMutableTreeNode synRoot = (DefaultMutableTreeNode)tn.getChildAt(4);
		DefaultMutableTreeNode typeRoot = (DefaultMutableTreeNode)tn.getChildAt(5);
		DefaultMutableTreeNode unitRoot = (DefaultMutableTreeNode)tn.getChildAt(6);
		
		DefaultMutableTreeNode pe_Comments = (DefaultMutableTreeNode)commentRoot.getChildAt(0);
		DefaultMutableTreeNode pe_desc = (DefaultMutableTreeNode)descRoot.getChildAt(0);
		DefaultMutableTreeNode pe_id = (DefaultMutableTreeNode)idRoot.getChildAt(0);
		DefaultMutableTreeNode pe_mo = (DefaultMutableTreeNode)moRoot.getChildAt(0);
		DefaultMutableTreeNode pe_type = (DefaultMutableTreeNode)typeRoot.getChildAt(0);
		DefaultMutableTreeNode pe_unit = (DefaultMutableTreeNode)unitRoot.getChildAt(0);
		
		addElementsFromTreeNode(synRoot,theProfileElement.getSynonyms());
		
		theProfileElement.setName((String)tn.getUserObject());
		theProfileElement.setComments((String)pe_Comments.getUserObject());
		theProfileElement.setDescription((String)pe_desc.getUserObject());
		theProfileElement.setID((String)pe_id.getUserObject());
		theProfileElement.setMaxOccurrence(Integer.parseInt((String)pe_mo.getUserObject()));
		theProfileElement.setType((String)pe_type.getUserObject());
		theProfileElement.setUnit((String)pe_unit.getUserObject());
		
		return theProfileElement;
		
		//return new RangedProfileElement(new Profile());
	}
	
	
	
	private DefaultTreeModel generateModelFromProfile(Profile p){
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Profile");
		DefaultMutableTreeNode resAttrRoot = new DefaultMutableTreeNode("Resource Attributes");
		DefaultMutableTreeNode profAttrRoot = new DefaultMutableTreeNode("Profile Attributes");
		DefaultMutableTreeNode profElemRoot = new DefaultMutableTreeNode("Profile Elements");
		
		//do the Profile Attributes here
		//children List
		//id String
		//parent String
		//regAuthority String
		//revisionNotes List
		//securityType String
		//statusID String
		//type String
		//version String
		
		DefaultMutableTreeNode profAttr_children = new DefaultMutableTreeNode("Children");
		
		for(Iterator i = p.getProfileAttributes().getChildren().iterator(); i.hasNext(); ){
			String theChild = (String)i.next();
			
			DefaultMutableTreeNode profAttr_childN = new DefaultMutableTreeNode(theChild);
			profAttr_children.add(profAttr_childN);
		}
		
		
		DefaultMutableTreeNode profAttr_id = new DefaultMutableTreeNode("Id");
		profAttr_id.add(new DefaultMutableTreeNode(p.getProfileAttributes().getID()));

		DefaultMutableTreeNode profAttr_parent = new DefaultMutableTreeNode("Parent");
		profAttr_parent.add(new DefaultMutableTreeNode(p.getProfileAttributes().getParent()));		

		DefaultMutableTreeNode profAttr_regAuth = new DefaultMutableTreeNode("Registration Authority");
		profAttr_regAuth.add(new DefaultMutableTreeNode(p.getProfileAttributes().getRegAuthority()));		
		
		DefaultMutableTreeNode profAttr_revNotes = new DefaultMutableTreeNode("Revision Notes");
		
		for(Iterator i = p.getProfileAttributes().getRevisionNotes().iterator(); i.hasNext(); ){
			String revNoteString = (String)i.next();
			
			DefaultMutableTreeNode revNote_Child = new DefaultMutableTreeNode(revNoteString);
			profAttr_revNotes.add(revNote_Child);
		}

		DefaultMutableTreeNode profAttr_securityType = new DefaultMutableTreeNode("Security Type");
		profAttr_securityType.add(new DefaultMutableTreeNode(p.getProfileAttributes().getSecurityType()));		
		
		DefaultMutableTreeNode profAttr_statusID = new DefaultMutableTreeNode("Status ID");
		profAttr_statusID.add(new DefaultMutableTreeNode(p.getProfileAttributes().getStatusID()));		
		
		DefaultMutableTreeNode profAttr_type = new DefaultMutableTreeNode("Type");
		profAttr_type.add(new DefaultMutableTreeNode(p.getProfileAttributes().getType()));		
		
		DefaultMutableTreeNode profAttr_version = new DefaultMutableTreeNode("Version");
		profAttr_version.add(new DefaultMutableTreeNode(p.getProfileAttributes().getVersion()));		
		
		profAttrRoot.add(profAttr_children);
		profAttrRoot.add(profAttr_id);
		profAttrRoot.add(profAttr_parent);
		profAttrRoot.add(profAttr_regAuth);
		profAttrRoot.add(profAttr_revNotes);
		profAttrRoot.add(profAttr_securityType);
		profAttrRoot.add(profAttr_statusID);
		profAttrRoot.add(profAttr_type);
		profAttrRoot.add(profAttr_version);
		
		//resource attributes
		//aggregation - string
		//class - string
		//contexts - list
		//contributors - list
		//coverages - list
		//creators - list
		//dates - list
		//description - string
		//formats - list
		//identifier - string
		//languages - list
		//locations - list
		//publishers - list
		//relations - list
		//rights - list
		//sources - list
		//subjects - list
		//title - string
		//types - list
		
		DefaultMutableTreeNode resAttr_aggregation = new DefaultMutableTreeNode("Aggregation");
		resAttr_aggregation.add(new DefaultMutableTreeNode(p.getResourceAttributes().getResAggregation()));		

		DefaultMutableTreeNode resAttr_class = new DefaultMutableTreeNode("Class");
		resAttr_class.add(new DefaultMutableTreeNode(p.getResourceAttributes().getResClass()));		
		
		
		DefaultMutableTreeNode resAttr_contexts = new DefaultMutableTreeNode("Contexts");
		
		for(Iterator i = p.getResourceAttributes().getResContexts().iterator(); i.hasNext(); ){
			String theContext = (String)i.next();
			
			DefaultMutableTreeNode resAttr_contextN = new DefaultMutableTreeNode(theContext);
			resAttr_contexts.add(resAttr_contextN);
		}

		DefaultMutableTreeNode resAttr_contributors = new DefaultMutableTreeNode("Contributors");
		
		for(Iterator i = p.getResourceAttributes().getContributors().iterator(); i.hasNext(); ){
			String theContributor = (String)i.next();
			
			DefaultMutableTreeNode resAttr_contribN = new DefaultMutableTreeNode(theContributor);
			resAttr_contributors.add(resAttr_contribN);
		}
		
		DefaultMutableTreeNode resAttr_coverages = new DefaultMutableTreeNode("Coverages");
		
		for(Iterator i = p.getResourceAttributes().getCoverages().iterator(); i.hasNext(); ){
			String theCoverage = (String)i.next();
			
			DefaultMutableTreeNode resAttr_coverageN= new DefaultMutableTreeNode(theCoverage);
			resAttr_coverages.add(resAttr_coverageN);
		}
		
		DefaultMutableTreeNode resAttr_creators = new DefaultMutableTreeNode("Creators");
		
		for(Iterator i = p.getResourceAttributes().getCreators().iterator(); i.hasNext(); ){
			String theCreator = (String)i.next();
			
			DefaultMutableTreeNode resAttr_creatorN = new DefaultMutableTreeNode(theCreator);
			resAttr_creators.add(resAttr_creatorN);
		}

		DefaultMutableTreeNode resAttr_dates = new DefaultMutableTreeNode("Dates");
		
		for(Iterator i = p.getResourceAttributes().getDates().iterator(); i.hasNext(); ){
			String theDate = (String)i.next();
			
			DefaultMutableTreeNode resAttr_dateN = new DefaultMutableTreeNode(theDate);
			resAttr_dates.add(resAttr_dateN);
		}
	
		DefaultMutableTreeNode resAttr_description = new DefaultMutableTreeNode("Description");
		resAttr_description.add(new DefaultMutableTreeNode(p.getResourceAttributes().getDescription()));		

		DefaultMutableTreeNode resAttr_formats = new DefaultMutableTreeNode("Formats");
		
		for(Iterator i = p.getResourceAttributes().getFormats().iterator(); i.hasNext(); ){
			String theFormat = (String)i.next();
			
			DefaultMutableTreeNode resAttr_formatN = new DefaultMutableTreeNode(theFormat);
			resAttr_formats.add(resAttr_formatN);
		}
		
		DefaultMutableTreeNode resAttr_identifier = new DefaultMutableTreeNode("Identifier");
		resAttr_identifier.add(new DefaultMutableTreeNode(p.getResourceAttributes().getIdentifier()));		

		DefaultMutableTreeNode resAttr_languages = new DefaultMutableTreeNode("Languages");
		
		for(Iterator i = p.getResourceAttributes().getLanguages().iterator(); i.hasNext(); ){
			String theLanguage = (String)i.next();
			
			DefaultMutableTreeNode resAttr_langN = new DefaultMutableTreeNode(theLanguage);
			resAttr_languages.add(resAttr_langN);
		}
		
		DefaultMutableTreeNode resAttr_locations = new DefaultMutableTreeNode("Resource Locations");
		
		for(Iterator i = p.getResourceAttributes().getResLocations().iterator(); i.hasNext(); ){
			String theLoc = (String)i.next();
			
			DefaultMutableTreeNode resAttr_locN = new DefaultMutableTreeNode(theLoc);
			resAttr_locations.add(resAttr_locN);
		}

		DefaultMutableTreeNode resAttr_publishers = new DefaultMutableTreeNode("Publishers");
		
		for(Iterator i = p.getResourceAttributes().getPublishers().iterator(); i.hasNext(); ){
			String thePublisher = (String)i.next();
			
			DefaultMutableTreeNode resAttr_pubN = new DefaultMutableTreeNode(thePublisher);
			resAttr_publishers.add(resAttr_pubN);
		}
		
		DefaultMutableTreeNode resAttr_relations = new DefaultMutableTreeNode("Relations");
		
		for(Iterator i = p.getResourceAttributes().getRelations().iterator(); i.hasNext(); ){
			String theRelation = (String)i.next();
			
			DefaultMutableTreeNode resAttr_relationN = new DefaultMutableTreeNode(theRelation);
			resAttr_relations.add(resAttr_relationN);
		}

		
		DefaultMutableTreeNode resAttr_rights = new DefaultMutableTreeNode("Rights");
		
		for(Iterator i = p.getResourceAttributes().getRights().iterator(); i.hasNext(); ){
			String theRight = (String)i.next();
			
			DefaultMutableTreeNode resAttr_rightN = new DefaultMutableTreeNode(theRight);
			resAttr_rights.add(resAttr_rightN);
		}

		DefaultMutableTreeNode resAttr_sources = new DefaultMutableTreeNode("Sources");
		
		for(Iterator i = p.getResourceAttributes().getSources().iterator(); i.hasNext(); ){
			String theSource = (String)i.next();
			
			DefaultMutableTreeNode resAttr_sourceN = new DefaultMutableTreeNode(theSource);
			resAttr_sources.add(resAttr_sourceN);
		}

		DefaultMutableTreeNode resAttr_subjects = new DefaultMutableTreeNode("Subjects");
		
		for(Iterator i = p.getResourceAttributes().getSubjects().iterator(); i.hasNext(); ){
			String theSubject = (String)i.next();
			
			DefaultMutableTreeNode resAttr_subjectN = new DefaultMutableTreeNode(theSubject);
			resAttr_subjects.add(resAttr_subjectN);
		}

		DefaultMutableTreeNode resAttr_title = new DefaultMutableTreeNode("Title");
		resAttr_title.add(new DefaultMutableTreeNode(p.getResourceAttributes().getTitle()));		
	
		DefaultMutableTreeNode resAttr_types = new DefaultMutableTreeNode("Types");
		
		for(Iterator i = p.getResourceAttributes().getTypes().iterator(); i.hasNext(); ){
			String theType = (String)i.next();
			
			DefaultMutableTreeNode resAttr_typeN = new DefaultMutableTreeNode(theType);
			resAttr_types.add(resAttr_typeN);
		}
	

		resAttrRoot.add(resAttr_aggregation);
		resAttrRoot.add(resAttr_class);
		resAttrRoot.add(resAttr_contexts);
		resAttrRoot.add(resAttr_contributors);
		resAttrRoot.add(resAttr_coverages);
		resAttrRoot.add(resAttr_creators);
		resAttrRoot.add(resAttr_dates);
		resAttrRoot.add(resAttr_description);
		resAttrRoot.add(resAttr_formats);
		resAttrRoot.add(resAttr_identifier);
		resAttrRoot.add(resAttr_languages);
		resAttrRoot.add(resAttr_locations);
		resAttrRoot.add(resAttr_publishers);
		resAttrRoot.add(resAttr_relations);
		resAttrRoot.add(resAttr_rights);
		resAttrRoot.add(resAttr_sources);
		resAttrRoot.add(resAttr_subjects);
		resAttrRoot.add(resAttr_title);
		resAttrRoot.add(resAttr_types);

        for(Iterator i = p.getProfileElements().keySet().iterator(); i.hasNext(); ){
        	String peKey = (String)i.next();
        	
        	ProfileElement theProfileElement = (ProfileElement)p.getProfileElements().get(peKey);
        	DefaultMutableTreeNode thePENode = new DefaultMutableTreeNode(theProfileElement.getName());
        	DefaultMutableTreeNode theCommentsRoot = new DefaultMutableTreeNode("Comments");
        	DefaultMutableTreeNode theComments = new DefaultMutableTreeNode(theProfileElement.getComments());
        	
        	theCommentsRoot.add(theComments);
        	
        	DefaultMutableTreeNode theDesc = new DefaultMutableTreeNode(theProfileElement.getDescription());
        	DefaultMutableTreeNode theDescRoot = new DefaultMutableTreeNode("Description");
        	
        	theDescRoot.add(theDesc);
        	
        	
        	
        	DefaultMutableTreeNode theID = new DefaultMutableTreeNode(theProfileElement.getID());
        	DefaultMutableTreeNode theIDRoot = new DefaultMutableTreeNode("ID");
        	
        	theIDRoot.add(theID);
        	
        	DefaultMutableTreeNode theMO = new DefaultMutableTreeNode(new Integer(theProfileElement.getMaxOccurrence()).toString());
        	DefaultMutableTreeNode theMORoot = new DefaultMutableTreeNode("Max Occurence");
        	theMORoot.add(theMO);
        	
        	DefaultMutableTreeNode theSynonyms = new DefaultMutableTreeNode("Synonyms");
        	
        	for(Iterator i2 = theProfileElement.getSynonyms().iterator(); i2.hasNext(); ){
        		String theSynonym = (String)i2.next();
        		DefaultMutableTreeNode sNode = new DefaultMutableTreeNode(theSynonym);
        		theSynonyms.add(sNode);
        	}

        	DefaultMutableTreeNode theType = new DefaultMutableTreeNode(theProfileElement.getType());
        	DefaultMutableTreeNode theTypeRoot = new DefaultMutableTreeNode("Type");
        	theTypeRoot.add(theType);
        	
        	
        	DefaultMutableTreeNode theUnit = new DefaultMutableTreeNode(theProfileElement.getUnit());
        	DefaultMutableTreeNode theUnitRoot = new DefaultMutableTreeNode("Unit");
        	theUnitRoot.add(theUnit);
        	
        	thePENode.add(theCommentsRoot);
        	thePENode.add(theDescRoot);
        	thePENode.add(theIDRoot);
        	thePENode.add(theMORoot);
        	thePENode.add(theSynonyms);
        	thePENode.add(theTypeRoot);
        	thePENode.add(theUnitRoot);
        	
        	profElemRoot.add(thePENode);
        }
		
		root.add(profAttrRoot);
		root.add(resAttrRoot);
		root.add(profElemRoot);
		
		return new DefaultTreeModel(root);		
		
		
	}

	/** Auto-generated event handler method */
	protected void openFileMenuItemActionPerformed(ActionEvent evt){
		//TODO add your handler code here
		
	    String filename = File.separator+"tmp";
	    JFileChooser fc = new JFileChooser(new File(filename));
	    
	    // Show open dialog; this method does not return until the dialog is closed
	    int decision = fc.showOpenDialog(this);
	    
	    if(decision != JFileChooser.APPROVE_OPTION){
	    	return; 
	    }
	    
	    File selFile = fc.getSelectedFile();
	    
	    //create a buffered reader, read it in, create a new profile from that file
	    FileReader fr = null;
	    
	    try{
	    	fr = new FileReader(selFile);
	    }
	    catch(FileNotFoundException fne){
	    	fne.printStackTrace();
	    	System.out.println(fne.getMessage());
	    }
	    
	    char [] buf = new char[256];
	    StringBuffer sb = new StringBuffer();
	    
	    int numRead = -1;
	    
	    try{
		    while((numRead = fr.read(buf,0,256)) != -1){
		      sb.append(buf,0,numRead);
		      buf = new char[256];
		    }
	    }
	    catch(IOException ioe){
	    	ioe.printStackTrace();
	    	System.out.println(ioe.getMessage());	    	
	    }
	    
	    System.out.println("Read in "+sb.toString());
	    try{
	    	createdProfile = new Profile(sb.toString());        
	    	jEditorPane1.setText(new ProfilePrinter(createdProfile,"http://oodt.jpl.nasa.gov/dtd/prof.dtd").toXMLString());
			
			getJTree1().setModel(generateModelFromProfile(createdProfile));
			//getJTree1().addMouseListener(new LeafListener(getJTree1()));
	    }catch(SAXException se){
	    	se.printStackTrace();
	    	System.out.println(se.getMessage());
	    }

	    
	}

	/** Auto-generated event handler method */
	protected void jButton1ActionPerformed(ActionEvent evt){
		//TODO add your handler code here
		
		//basically we want to set the createdProfile to be generated from the current tree model
		//then we want to set the jeditor pane to have the createdProfileText
		
		createdProfile = generateProfileFromModel(getJTree1().getModel());
		//jEditorPane1.setText(createdProfile.toString());
		jEditorPane1.setText(new ProfilePrinter(createdProfile,"http://oodt.jpl.nasa.gov/dtd/prof.dtd").toXMLString());
		
	}

	/** Auto-generated event handler method */
	protected void saveMenuItemActionPerformed(ActionEvent evt){
		//TODO add your handler code here
	    
		
	    JFileChooser fc = new JFileChooser();
	    int decision = fc.showSaveDialog(this);
	    
	    if(decision != JFileChooser.APPROVE_OPTION){
	    	return; 
	    }
	    
	    //first thing -- set the created profile to the current tree
	    createdProfile = generateProfileFromModel(getJTree1().getModel());
	    
		File file = fc.getSelectedFile();
        FileOutputStream fos = null;
        
        try{
        	System.out.println("Trying to write to "+file.getAbsolutePath());
        	fos = new FileOutputStream (file.getAbsolutePath());
        	fos.write(new ProfilePrinter(createdProfile,"http://oodt.jpl.nasa.gov/dtd/prof.dtd").toXMLString().getBytes());
        }
        catch(FileNotFoundException fne){
        	fne.printStackTrace();
        	System.out.println(fne.getMessage());
        }
        catch(IOException ioe){
        	ioe.printStackTrace();
        	System.out.println(ioe.getMessage());
        }
        finally{
        	try{
        		fos.close();
        	}catch(Exception ignore){
        		//ignore
        	}
        }

	}
}

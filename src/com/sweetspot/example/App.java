package com.sweetspot.example;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

import org.apache.http.client.ClientProtocolException;

import com.sweetspot.api.models.Scorecard;
import com.sweetspot.api.models.ScorecardFolder;
import com.sweetspot.api.models.ScorecardItem;
import com.sweetspot.api.models.ScorecardPanel;
import com.sweetspot.api.models.ScorecardTab;
import com.sweetspot.api.models.ScorecardTabElement;
import com.sweetspot.api.models.User;
import com.sweetspot.api.services.SweetspotService;

public class App {
  
  private SweetspotService sweetspot;
  
  public App() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, IOException {
    this.sweetspot = this.buildSweetspotService();
  }
  
  public Properties getPropertiesCredentials() throws IOException {
    InputStream in = this.getClass().getResourceAsStream("/secret.properties");
    Properties credentials = new Properties();
    credentials.load(in);
    return credentials;
  }
  
  public SweetspotService buildSweetspotService() throws IOException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
    Properties credentials = this.getPropertiesCredentials();
    String user = credentials.getProperty("user");
    String password = credentials.getProperty("password");
    
    return new SweetspotService(user, password);
  }
  
  public void run() throws ClientProtocolException, IOException {
    User currentUser = sweetspot.getUser();
    Collection<ScorecardItem> scorecardItems = this.getAllScorecardItemsByUser(currentUser);
    Iterator<ScorecardItem> iterator = scorecardItems.iterator();
    while(iterator.hasNext()) {
      ScorecardItem scorecardItem = iterator.next();
      if(scorecardItem.getArrow().equalsIgnoreCase("down")) {
        System.out.println("Hey! You should work hard here:");
        System.out.println(scorecardItem);
        System.out.println("");
      }      
    }
  }
  
  public Collection<ScorecardItem> getAllScorecardItemsByUser(User user) throws ClientProtocolException, IOException {
    Collection<ScorecardItem> allScorecardItems = new ArrayList<ScorecardItem>();
    if(user.getScorecards() != null) {
      Iterator<Scorecard> iterator = user.getScorecards().iterator();
      while(iterator.hasNext()) {
        Scorecard scorecardDefinition = iterator.next();
        Scorecard detailedScorecard = sweetspot.getScorecard(user.getId(), scorecardDefinition.getId());
        allScorecardItems.addAll(getAllScorecardItemsByScorecard(detailedScorecard));
      }
    }
    
    return allScorecardItems;
  }
  
  public Collection<ScorecardItem> getAllScorecardItemsByScorecard(Scorecard scorecard) throws ClientProtocolException, IOException {
    Collection<ScorecardItem> allScorecardItems = new ArrayList<ScorecardItem>();
    Iterator<ScorecardTab> iterator = scorecard.getScorecardTabs().iterator();
    while(iterator.hasNext()) {
      ScorecardTab tab = iterator.next();
      allScorecardItems.addAll(this.getAllScorecardItemsByScorecardTab(tab));
    }
    
    return allScorecardItems;
  }
  
  public Collection<ScorecardItem> getAllScorecardItemsByScorecardTab(ScorecardTab tab) throws ClientProtocolException, IOException {
    Collection<ScorecardItem> allScorecardItems = new ArrayList<ScorecardItem>();
    
    Iterator<ScorecardTabElement> iterator = tab.getScorecardTabElements().iterator();
    while(iterator.hasNext()) {
      ScorecardTabElement tabElement = iterator.next();
      if(tabElement.getType().equalsIgnoreCase("panel")) {
        ScorecardPanel scorecardPanel = sweetspot.getScorecardPanel(tabElement.getId());
        allScorecardItems.addAll(this.getAllScorecardItemsByScorecardPanel(scorecardPanel));
      }
    }
    
    return allScorecardItems;
  }
  
  public Collection<ScorecardItem> getAllScorecardItemsByScorecardPanel(ScorecardPanel scorecardPanel) {
    Collection<ScorecardItem> allScorecardItems = new ArrayList<ScorecardItem>();
    ScorecardFolder rootFolder = scorecardPanel.getRootFolder();
    allScorecardItems.addAll(this.getAllScorecardItemsByScorecardFolder(rootFolder));
    
    return allScorecardItems;
  }
  
  public Collection<ScorecardItem> getAllScorecardItemsByScorecardFolder(ScorecardFolder folder) {
    Collection<ScorecardItem> allScorecardItems = folder.getScorecardItems();
    
    Iterator<ScorecardFolder> iterator = folder.getChildren().iterator();
    while(iterator.hasNext()) {
      ScorecardFolder child = iterator.next();      
      allScorecardItems.addAll(this.getAllScorecardItemsByScorecardFolder(child));
    }
    
    return allScorecardItems;
  }
  
  /* ===========================
   *  MAIN METHOD:
   * ===========================
   */
	public static void main(String[] args) {	  
		try {
		  App app = new App();
			app.run();
		} catch (Exception e) {
			System.err.print(e.getMessage());
		}
	}
}
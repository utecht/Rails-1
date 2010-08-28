package rails.algorithms;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;


import rails.game.ConfigurableComponentI;
import rails.game.ConfigurationException;
import rails.game.GameManagerI;
import rails.game.state.HashSetState;
import rails.util.LocalText;
import rails.util.Tag;

/**
 * Coordinates and stores all elements related to revenue calulcation,
 * which are permanent.
 * The conversion of Rails elements is in the responsibility of the RevenueAdapter.
 * For each GameManager instance only one RevenueManager is created.
 * 
 * @author freystef
 *
 */

public final class RevenueManager implements ConfigurableComponentI {

    protected static Logger log =
        Logger.getLogger(RevenueManager.class.getPackage().getName());

    
    private final HashSetState<NetworkGraphModifier> graphModifiers;
    private final HashSetState<RevenueStaticModifier> staticModifiers;
    private final HashSetState<RevenueDynamicModifier> dynamicModifiers;
    private final HashSet<ConfigurableComponentI> configurableModifiers;

    public RevenueManager() {
        graphModifiers = new HashSetState<NetworkGraphModifier>("NetworkGraphModifiers"); 
        staticModifiers = new HashSetState<RevenueStaticModifier>("RevenueStaticModifiers"); 
        dynamicModifiers = new HashSetState<RevenueDynamicModifier>("RevenueDynamicModifiers");
        configurableModifiers = new HashSet<ConfigurableComponentI>();
    }
    
    public void configureFromXML(Tag tag) throws ConfigurationException {
        
        // define modifiers
        List<Tag> modifierTags = tag.getChildren("Modifier");
        
        if (modifierTags != null) {
            for (Tag modifierTag:modifierTags) {
                // get classname
                String className = modifierTag.getAttributeAsString("class");
                if (className == null) {
                    throw new ConfigurationException(LocalText.getText(
                            "ComponentHasNoClass", "Modifier"));
                }
                // create modifier
                Object modifier;
                try {
                    modifier = Class.forName(className).newInstance();
                } catch (Exception e) {
                    throw new ConfigurationException(LocalText.getText(
                            "ClassCannotBeInstantiated", className), e);
                }
                boolean isModifier = false;
                // add them to the revenueManager
                if (modifier instanceof NetworkGraphModifier) {
                    graphModifiers.add((NetworkGraphModifier)modifier);
                    isModifier = true;
                    log.info("Added as graph modifier = " + className);
                }
                if (modifier instanceof RevenueStaticModifier) {
                    staticModifiers.add((RevenueStaticModifier)modifier);
                    isModifier = true;
                    log.info("Added as static modifier = " + className);
                }
                if (modifier instanceof RevenueDynamicModifier) {
                    dynamicModifiers.add((RevenueDynamicModifier)modifier);
                    isModifier = true;
                    log.info("Added as dynamic modifier = " + className);
                }
                if (!isModifier) {
                    throw new ConfigurationException(LocalText.getText(
                            "ClassIsNotAModifier", className));
                }
                if (isModifier && modifier instanceof ConfigurableComponentI) {
                    configurableModifiers.add((ConfigurableComponentI)modifier);
                }
            }
        }

    }

    public void finishConfiguration(GameManagerI parent)
            throws ConfigurationException {
        for (ConfigurableComponentI modifier:configurableModifiers) {
                modifier.finishConfiguration(parent);
        }
    }
    
    public void addStaticModifier(RevenueStaticModifier modifier) {
        staticModifiers.add(modifier);
        log.info("Revenue Manager: Added static modifier " + modifier);
    }
    
    public boolean removeStaticModifier(RevenueStaticModifier modifier) {
        boolean result = staticModifiers.remove(modifier);
        if (result) {
            log.info("RevenueManager: Removed static modifier " + modifier);
        } else {
            log.info("RevenueManager: Cannot remove" + modifier);
        }
        return result;
    }

    public void addGraphModifier(NetworkGraphModifier modifier) {
        graphModifiers.add(modifier);
        log.info("Revenue Manager: Added graph modifier " + modifier);
    }
    
    public boolean removeGraphModifier(NetworkGraphModifier modifier) {
        boolean result = graphModifiers.remove(modifier);
        if (result) {
            log.info("RevenueManager: Removed graph modifier " + modifier);
        } else {
            log.info("RevenueManager: Cannot remove" + modifier);
        }
        return result;
    }

    public void addDynamicModifier(RevenueDynamicModifier modifier) {
        dynamicModifiers.add(modifier);
        log.info("Revenue Manager: Added dynamic modifier " + modifier);
    }
    
    public boolean removeDynamicModifier(RevenueDynamicModifier modifier) {
        boolean result = dynamicModifiers.remove(modifier);
        if (result) {
            log.info("RevenueManager: Removed dynamic modifier " + modifier);
        } else {
            log.info("RevenueManager: Cannot remove" + modifier);
        }
        return result;
    }

    void callGraphModifiers(NetworkGraphBuilder graphBuilder) {
        for (NetworkGraphModifier modifier:graphModifiers.viewSet()) {
            modifier.modifyGraph(graphBuilder);
        }
    }
    
    void callStaticModifiers(RevenueAdapter revenueAdapter) {
        for (RevenueStaticModifier modifier:staticModifiers.viewSet()) {
            modifier.modifyCalculator(revenueAdapter);
        }
    }

    Set<RevenueDynamicModifier> callDynamicModifiers(RevenueAdapter revenueAdapter) {
        Set<RevenueDynamicModifier> activeModifiers = new HashSet<RevenueDynamicModifier>();
        for (RevenueDynamicModifier modifier:dynamicModifiers.viewSet()) {
            if (modifier.prepareModifier(revenueAdapter))
                activeModifiers.add(modifier);
        }
        return activeModifiers;
    }

}
package nl.tudelft.tbm.eeni.owl2java.model.jmodel;

import nl.tudelft.tbm.eeni.owl2java.model.jmodel.utils.LogUtils;
import nl.tudelft.tbm.eeni.owl2java.utils.IReporting;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class JCardinalityRestriction extends JBaseRestriction implements IReporting {

    @SuppressWarnings("unused")
    private static Log log = LogFactory.getLog(JCardinalityRestriction.class);

    private int maxCardinality = -1;
    private int minCardinality = 0;

    // multipleXXX = true -> we need accessor methods for this type;
    private boolean multipleEnabled = true;
    private boolean singleEnabled = false;
    // deprecated status is set depending on the max and min values
    // multipleDeprecated == true does not mean multipe = true
    private boolean multipleDeprecated = false;
    private boolean singleDeprecated = false;

    public JCardinalityRestriction(JClass onClass, JProperty onProperty) {
        super(onClass, onProperty);
        // disable multiple stuff for functionals
        if (onProperty.isFunctional()) {
            multipleEnabled = false;
            singleEnabled = true;
        }
    }

    public JCardinalityRestriction clone() {
        JCardinalityRestriction restriction = new JCardinalityRestriction(onClass, onProperty);
        restriction.isEmpty = isEmpty;
        restriction.maxCardinality = maxCardinality;
        restriction.minCardinality = minCardinality;
        restriction.multipleEnabled = multipleEnabled;
        restriction.multipleDeprecated = multipleDeprecated;
        restriction.singleEnabled = singleEnabled;
        restriction.singleDeprecated = singleDeprecated;

        return restriction;
    }

    public boolean equalsIgnoreDeprecated(Object other) {
        if (!(other instanceof JCardinalityRestriction))
            return false;
        JCardinalityRestriction cr = (JCardinalityRestriction) other;
        if (!(isEmpty == cr.isEmpty))
            return false;
        if (!(maxCardinality == cr.maxCardinality))
            return false;
        if (!(minCardinality == cr.minCardinality))
            return false;
        if (!(multipleEnabled == cr.multipleEnabled))
            return false;
        if (!(singleEnabled == cr.singleEnabled))
            return false;
        return true;
    }

    public boolean equals(Object other) {
        if (!equalsIgnoreDeprecated(other))
            return false;
        JCardinalityRestriction cr = (JCardinalityRestriction) other;
        if (!(multipleDeprecated == cr.multipleDeprecated))
            if (!(singleDeprecated == cr.singleDeprecated))
                return false;
        return true;
    }

    public void mergeParent(JCardinalityRestriction parent) {
        // empty parent cardinality restrictions (aka no restrictions) are ignored
        if (!parent.isEmpty) {
            setMaxCardinality(parent.getMaxCardinality());
            setMinCardinality(parent.getMinCardinality());
            // set the enabled status
            multipleEnabled = multipleEnabled || parent.multipleEnabled;
            singleEnabled = singleEnabled || parent.singleEnabled;
            // set the deprecated and enabled status on merge cardinalities
            updateDeprecatedStatus();
        }
    }

    protected void updateDeprecatedStatus() {
        if (maxCardinality == 1) {
            multipleDeprecated = true;
            singleEnabled = true;
        }
        if (maxCardinality == 0 || (minCardinality > maxCardinality)) {
            multipleDeprecated = true;
            singleDeprecated = true;
        }
    }

    public void setMaxCardinality(int max) {
        if (maxCardinality == -1)
            maxCardinality = max;
        else {
            if (maxCardinality > max)
                maxCardinality = max;
        }
        isEmpty = false;
        updateDeprecatedStatus();
    }

    public void setMinCardinality(int min) {
        if (minCardinality == 0)
            minCardinality = min;
        else {
            if (minCardinality < min)
                minCardinality = min;
        }
        isEmpty = false;
        updateDeprecatedStatus();
    }

    public void setCardinality(int cardinality) {
        setMaxCardinality(cardinality);
        setMinCardinality(cardinality);
        updateDeprecatedStatus();
    }


    @Override
    public String getJModelReport() {
        String ret = LogUtils.toLogName(this) + ": ";
        if (isEmpty)
            return ret + "Empty cardinality restriction";
        ret += "Max " + maxCardinality + ", Min " + minCardinality + "; ";
        ret += "multiple " + multipleEnabled + ", deprecated " + multipleDeprecated + "; ";
        ret += "single " + singleEnabled + ", deprecate " + singleDeprecated + "; ";
        return ret;
    }

    public int getMaxCardinality() {
        return maxCardinality;
    }

    public int getMinCardinality() {
        return minCardinality;
    }

    public boolean isMultipleEnabled() {
        return multipleEnabled;
    }

    public boolean isSingleEnabled() {
        return singleEnabled;
    }

    public boolean isMultipleDeprecated() {
        return multipleDeprecated;
    }

    public boolean isSingleDeprecated() {
        return singleDeprecated;
    }

    public void setMultipleDeprecated(boolean multipleDeprecated) {
        this.multipleDeprecated = multipleDeprecated;
    }

    public void setSingleDeprecated(boolean singleDeprecated) {
        this.singleDeprecated = singleDeprecated;
    }

    public void setMultipleEnabled(boolean multipleEnabled) {
        this.multipleEnabled = multipleEnabled;
    }

    public void setSingleEnabled(boolean singleEnabled) {
        this.singleEnabled = singleEnabled;
    }

}

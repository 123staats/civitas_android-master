package eu.mlab.civitas.android.model;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by christophstanik on 3/14/17.
 */

public class SupportedLicenses {
    public static final String LICENSES_EXPLANATION_LINK = "https://creativecommons.org/licenses/?lang=de";
    public static final String CC_BY = "CC BY 4.0";
    public static final String CC_BY_SA = "CC BY-SA 4.0";
    public static final String CC_BY_ND = "CC BY-ND 4.0";
    public static final String CC_BY_NC = "CC BY-NC 4.0";
    public static final String CC_BY_NC_SA = "CC BY-NC-SA 4.0";
    public static final String CC_BY_NC_ND = "CC BY-ND 4.0";
    // legacy licenses
    public static final String CC_BY_v3 = "CC-BY 3.0";
    public static final String CC_BY_SA_v3 = "CC-BY-SA 3.0";
    public static final String CC_BY_ND_v3 = "CC-BY-ND 3.0";
    public static final String CC_BY_NC_v3 = "CC-BY-NC 3.0";
    public static final String CC_BY_NC_SA_v3 = "CC-BY-NC-SA 3.0";
    public static final String CC_BY_NC_ND_v3 = "CC-BY-ND 3.0";
    public static final HashMap<String, String> licenseLinkMap;
    static {
        licenseLinkMap = new HashMap();
        licenseLinkMap.put(CC_BY, "https://creativecommons.org/licenses/by/4.0/");
        licenseLinkMap.put(CC_BY_SA, "https://creativecommons.org/licenses/by-sa/4.0/");
        licenseLinkMap.put(CC_BY_ND, "https://creativecommons.org/licenses/by-nd/4.0/");
        licenseLinkMap.put(CC_BY_NC, "https://creativecommons.org/licenses/by-nc/4.0/");
        licenseLinkMap.put(CC_BY_NC_SA, "https://creativecommons.org/licenses/by-nc-sa/4.0/");
        licenseLinkMap.put(CC_BY_NC_ND, "https://creativecommons.org/licenses/by-nc-nd/4.0/");
        licenseLinkMap.put(CC_BY_v3, "https://creativecommons.org/licenses/by/3.0/");
        licenseLinkMap.put(CC_BY_SA_v3, "https://creativecommons.org/licenses/by-sa/3.0/");
        licenseLinkMap.put(CC_BY_ND_v3, "https://creativecommons.org/licenses/by-nd/3.0/");
        licenseLinkMap.put(CC_BY_NC_v3, "https://creativecommons.org/licenses/by-nc/3.0/");
        licenseLinkMap.put(CC_BY_NC_SA_v3, "https://creativecommons.org/licenses/by-nc-sa/3.0/");
        licenseLinkMap.put(CC_BY_NC_ND_v3, "https://creativecommons.org/licenses/by-nc-nd/3.0/");
    }
    // the user is just able to select the most recent CC licenses
    public static final ArrayList<String> licenseList;
    static {
        licenseList = new ArrayList<>();
        licenseList.add(CC_BY);
        licenseList.add(CC_BY_SA);
        licenseList.add(CC_BY_ND);
        licenseList.add(CC_BY_NC);
        licenseList.add(CC_BY_NC_SA);
        licenseList.add(CC_BY_NC_ND);
    }
}

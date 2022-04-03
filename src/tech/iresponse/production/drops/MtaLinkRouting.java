package tech.iresponse.production.drops;

import tech.iresponse.exceptions.DatabaseException;
import tech.iresponse.utils.Crypto;
import tech.iresponse.utils.Convertion;
import tech.iresponse.core.Application;

public class MtaLinkRouting {

    public static String createLinkRouting(String linkType, String action, int dropId, String type, int offerId, int vmtaId, int listId, int clientId) throws Exception {
        String str = "";
        try {
            switch (linkType) {
                case "routing":
                case "routing-bitly":
                case "routing-gcloud":
                case "routing-tinyurl":
                    str = action + "/" + dropId + "_" + type + "/" + (Application.checkAndgetInstance().getUser()).productionId + "/" + vmtaId + "/" + offerId + "/" + listId + "/" + clientId;
                    break;
                case "routing-encrypted":
                    str = action + "/" + dropId + "_" + type + "/" + (Application.checkAndgetInstance().getUser()).productionId + "/" + vmtaId + "/" + offerId + "/" + listId + "/" + clientId;
                    str = Crypto.Base64Encode(Convertion.crypt(str, Application.checkAndgetInstance().getSettings().getString("tracking_enc_key"))).replaceAll("=", "_");
                    break;
                case "attr":
                case "attr-bitly":
                case "attr-gcloud":
                case "attr-tinyurl":
                    str = "?act=" + action + "&pid=" + dropId + "_" + type + "&uid=" + (Application.checkAndgetInstance().getUser()).productionId + "&vid=" + vmtaId + "&ofid=" + offerId + "&lid=" + listId + "&cid=" + clientId;
                    break;
                case "attr-encrypted":
                    str = "?act=" + action + "&pid=" + dropId + "_" + type + "&uid=" + (Application.checkAndgetInstance().getUser()).productionId + "&vid=" + vmtaId + "&ofid=" + offerId + "&lid=" + listId + "&cid=" + clientId;
                    str = Crypto.Base64Encode(Convertion.crypt(str, Application.checkAndgetInstance().getSettings().getString("tracking_enc_key"))).replaceAll("=", "_");
                    break;
            }
        } catch (Exception e) {
            throw new DatabaseException("Could not generate link !", e);
        }
        return str;
    }
}

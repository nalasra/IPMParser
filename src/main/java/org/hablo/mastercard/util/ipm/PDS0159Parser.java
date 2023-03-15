package org.hablo.mastercard.util.ipm;

import org.hablo.mastercard.util.GenericTLVParser.GenericTag;
import org.hablo.mastercard.util.PDSParserSupport;

public class PDS0159Parser extends PDSParserSupport {
    public void parse(GenericTag tag) {
        tag.add(new GenericTag("1", tag.getValue().substring(0, 11), "sett_srv_agent_id_code"));
        tag.add(new GenericTag("2", tag.getValue().substring(11, 39), "sett_srv_trf_agent_account"));
        tag.add(new GenericTag("3", tag.getValue().substring(39, 40), "sett_srv_lvl_code"));
        tag.add(new GenericTag("4", tag.getValue().substring(40, 50), "sett_srv_id_code"));
        tag.add(new GenericTag("5", tag.getValue().substring(50, 51), "sett_srv_fx_rate_code"));
        tag.add(new GenericTag("6", tag.getValue().substring(51, 57), "recon_date"));
        tag.add(new GenericTag("7", tag.getValue().substring(57, 59), "recon_cycle"));
        tag.add(new GenericTag("8", tag.getValue().substring(59, 65), "sett_cycle"));
        tag.add(new GenericTag("9", tag.getValue().substring(65, 67), "sett_cycle"));
    }
}

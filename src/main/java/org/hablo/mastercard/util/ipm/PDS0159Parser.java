package org.hablo.mastercard.util.ipm;

import org.hablo.mastercard.util.GenericTLVParser.GenericTag;
import org.hablo.mastercard.util.PDSParserSupport;

public class PDS0159Parser extends PDSParserSupport {
    public void parse(GenericTag tag) {
        tag.add(new GenericTag(" <subfield id=\"1\" name=\"sett_srv_agent_id_code\" value=\"" + tag.getValue().substring(0, 11) + "\"/>",true));
        tag.add(new GenericTag(" <subfield id=\"2\" name=\"sett_srv_trf_agent_account\" value=\"" + tag.getValue().substring(11, 39) + "\"/>",true));
        tag.add(new GenericTag(" <subfield id=\"3\" name=\"sett_srv_lvl_code\" value=\"" + tag.getValue().substring(39, 40) + "\"/>",true));
        tag.add(new GenericTag(" <subfield id=\"4\" name=\"sett_srv_id_code\" value=\"" + tag.getValue().substring(40, 50) + "\"/>",true));
        tag.add(new GenericTag(" <subfield id=\"5\" name=\"sett_srv_fx_rate_code\" value=\"" + tag.getValue().substring(50, 51) + "\"/>",true));
        tag.add(new GenericTag(" <subfield id=\"6\" name=\"recon_date\" value=\"" + tag.getValue().substring(51, 57) + "\"/>",true));
        tag.add(new GenericTag(" <subfield id=\"7\" name=\"recon_cycle\" value=\"" + tag.getValue().substring(57, 59) + "\"/>",true));
        tag.add(new GenericTag(" <subfield id=\"8\" name=\"sett_cycle\" value=\"" + tag.getValue().substring(59, 65) + "\"/>",true));
        tag.add(new GenericTag(" <subfield id=\"9\" name=\"sett_cycle\" value=\"" + tag.getValue().substring(65, 67) + "\"/>",true));
    }
}

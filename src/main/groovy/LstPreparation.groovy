/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT040MI.LstPreparation
 * Description : Lst preparation
 * Date         Changed By   Description
 * 20231020     APACE        LOGM01-Mashup Libération BP
 */
public class LstPreparation extends ExtendM3Transaction {

    private final MIAPI mi
    private final DatabaseAPI database
    private final LoggerAPI logger
    private final ProgramAPI program

    public LstPreparation(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program) {
        this.mi = mi
        this.database = database
        this.logger = logger
        this.program = program
    }
    public void main() {
        Integer currentCompany = 0
        if (mi.in.get("CONO") == null) {
            currentCompany = (Integer)program.getLDAZD().CONO
        } else {
            currentCompany = mi.in.get("CONO")
        }
        String TEPA = ""
        if (mi.in.get("TEPA") != null) {
            TEPA = mi.in.get("TEPA")
        }
        String UCA0 = ""
        if (mi.in.get("UCA0") != null) {
            UCA0 = mi.in.get("UCA0")
        }
        Integer ORD1 = 0
        if (mi.in.get("ORD1") != null) {
            ORD1 = mi.in.get("ORD1")
        }
        Integer ORD2 = 0
        if (mi.in.get("ORD2") != null) {
            ORD2 = mi.in.get("ORD2")
        }
        String CUNO = ""
        if (mi.in.get("CUNO") != null) {
            CUNO = mi.in.get("CUNO")
        }
        String CONA = ""
        if (mi.in.get("CONA") != null) {
            CONA = mi.in.get("CONA")
        }
        Integer DSD1 = 0
        if (mi.in.get("DSD1") != null) {
            DSD1 = mi.in.get("DSD1")
        }
        Integer DSD2 = 0
        if (mi.in.get("DSD2") != null) {
            DSD2 = mi.in.get("DSD2")
        }
        String RID1 = ""
        if (mi.in.get("RID1") != null) {
            RID1 = mi.in.get("RID1")
        }
        String RID2 = ""
        if (mi.in.get("RID2") != null) {
            RID2 = mi.in.get("RID2")
        }

        ExpressionFactory expression = database.getExpressionFactory("MHDISH")
        expression = expression.eq("OQPGRS", "05")

        if(RID1!="" && RID2!=""){
            expression = expression.and(expression.between("OQRIDN", RID1,RID2))
        }else if(RID1!="" && RID2==""){
            expression = expression.and(expression.ge("OQRIDN", RID1))
        }

        if(DSD1!=0 && DSD2!=0){
            expression = expression.and(expression.between("OQDSDT", DSD1.toString(),DSD2.toString()))
        }else if(DSD1!=0 && DSD2==0){
            expression = expression.and(expression.ge("OQDSDT", DSD1.toString()))
        }
        if(CONA!=""){
            expression = expression.and(expression.eq("OQCONA", CONA.toString()))
        }
        DBAction query = database.table("MHDISH").index("00").matching(expression).selection("OQCONA","OQRIDN", "OQDLIX", "OQDSDT", "OQPGRS").build()
        DBContainer MHDISH = query.getContainer()
        MHDISH.set("OQCONO", currentCompany)

        Closure<?> outData = { DBContainer responseMHDISH ->
            mi.outData.put("CONA", responseMHDISH.get("OQCONA").toString())
            mi.outData.put("RIDN", responseMHDISH.get("OQRIDN").toString())
            mi.outData.put("DLIX", responseMHDISH.get("OQDLIX").toString())
            mi.outData.put("DSDT", responseMHDISH.get("OQDSDT").toString())
            mi.outData.put("PGRS", responseMHDISH.get("OQPGRS").toString())

            DBAction countryQuery = database.table("OCUSMA").index("00").selection("OKCUNM").build()
            DBContainer OCUSMA = countryQuery.getContainer()
            OCUSMA.set("OKCONO",currentCompany)
            OCUSMA.set("OKCUNO",responseMHDISH.get("OQCONA").toString())
            if (countryQuery.read(OCUSMA)) {
                mi.outData.put("CUNM", OCUSMA.get("OKCUNM").toString())
                ExpressionFactory expression2 = database.getExpressionFactory("OOHEAD")
                expression2 = expression2.eq("OAORNO", responseMHDISH.get("OQRIDN").toString())
                if(CUNO!=""){
                    expression2 = expression2.and(expression2.eq("OACUNO", CUNO.toString()))
                }
                if(TEPA!=""){
                    expression2 = expression2.and(expression2.eq("OATEPA", TEPA.toString()))
                }
                if(UCA0!=""){
                    expression2 = expression2.and(expression2.eq("OAUCA0", UCA0.toString()))
                }
                DBAction cdvQuery = database.table("OOHEAD").index("00").matching(expression2).selection("OACUNO","OAORTP","OACUNO","OAUCA0", "OAWCON", "OATEPA", "OAORDT","OAORSL", "OAORST", "OARESP").build()
                DBContainer OOHEAD = cdvQuery.getContainer()
                OOHEAD.set("OACONO",currentCompany)
                OOHEAD.set("OAORNO",responseMHDISH.get("OQRIDN").toString())

                if (cdvQuery.read(OOHEAD)) {
                    mi.outData.put("CUNO", OOHEAD.get("OACUNO").toString())
                    mi.outData.put("ORTP", OOHEAD.get("OAORTP").toString())
                    mi.outData.put("WCON", OOHEAD.get("OAWCON").toString())
                    mi.outData.put("TEPA", OOHEAD.get("OATEPA").toString())
                    mi.outData.put("ORDT", OOHEAD.get("OAORDT").toString())
                    mi.outData.put("ORSL", OOHEAD.get("OAORSL").toString())
                    mi.outData.put("ORST", OOHEAD.get("OAORST").toString())
                    mi.outData.put("RESP", OOHEAD.get("OARESP").toString())
                    if(ORD1!=0 && ORD2!=0){
                        if(ORD1 <= ((Integer)OOHEAD.get("OAORDT")) && ((Integer)OOHEAD.get("OAORDT")) <= ORD2){
                            mi.write()
                        }
                    }else if(ORD1!=0 && ORD2==0){
                        if(ORD1<=(Integer)OOHEAD.get("OAORDT")){
                            mi.write()
                        }
                    }else if(ORD1==0 && ORD2==0){
                        mi.write()
                    }
                }
            }
        }

        if(!query.readAll(MHDISH, 1, outData)){
            mi.error("L'enregistrement n'existe pas")
            return
        }

    }


}
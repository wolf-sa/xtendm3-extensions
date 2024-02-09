/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT860MI.AddLngTxt
 * Description : Add records to the CSLGHN table.
 * Date         Changed By   Description
 * 20220602     RENARN       REFX01 - Language handling
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

public class AddLngTxt extends ExtendM3Transaction {
  private final MIAPI mi
  private final LoggerAPI logger
  private final ProgramAPI program
  private final DatabaseAPI database
  private final SessionAPI session
  private final TransactionAPI transaction
  private final MICallerAPI miCaller
  private long dtid

  public AddLngTxt(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger, MICallerAPI miCaller) {
    this.mi = mi
    this.database = database
    this.program = program
    this.logger = logger
    this.miCaller = miCaller
  }

  public void main() {
    Integer currentCompany
    LocalDateTime timeOfCreation = LocalDateTime.now()
    if (mi.in.get("CONO") == null) {
      currentCompany = (Integer)program.getLDAZD().CONO
    } else {
      currentCompany = mi.in.get("CONO")
    }
    // Check language
    if(mi.in.get("LNCD") != null){
      DBAction countryQuery = database.table("CSYTAB").index("00").build()
      DBContainer CSYTAB = countryQuery.getContainer()
      CSYTAB.set("CTCONO",currentCompany)
      CSYTAB.set("CTSTCO",  "LNCD")
      CSYTAB.set("CTSTKY", mi.in.get("LNCD"))
      if (!countryQuery.read(CSYTAB)) {
        mi.error("Language " + mi.in.get("LNCD") + " n'existe pas")
        return
      }
    }
    // Get data id
    if(mi.in.get("DTID") != null){
      dtid = mi.in.get("DTID") as Integer
    } else {
      dtid = 0
      DBAction query = database.table("CSLGHN").index("00").reverse().build()
      DBContainer CSLGHN_0 = query.getContainer()
      CSLGHN_0.set("JLCONO", currentCompany)
      if (!query.readAll(CSLGHN_0, 1, 1, outData_CSLGHN)) {
        dtid = 1
      }
      // Update CSYNUM
      DBAction query_CSYNUM = database.table("CSYNUM").index("00").selection("CMCONO", "CMDIVI", "CMNBNR").build()
      DBContainer CSYNUM = query_CSYNUM.getContainer()
      CSYNUM.set("CMCONO", currentCompany)
      CSYNUM.set("CMDIVI", "")
      CSYNUM.set("CMFILE", "CSLGHN")
      if(!query_CSYNUM.readLock(CSYNUM, updateCallBack)){
      }
    }
    logger.debug("dtid = " + dtid)
    String iDescription =""
    String iShortDescription =""
    if(mi.in.get("TX60") != null)iDescription=mi.in.get("TX60")
    if(mi.in.get("TX15") != null)iShortDescription=mi.in.get("TX15")
    // Write CSLGHN
    DBAction query_CSLGHN = database.table("CSLGHN").index("00").build()
    DBContainer CSLGHN = query_CSLGHN.getContainer()
    CSLGHN.set("JLCONO", currentCompany)
    CSLGHN.set("JLDTID",  dtid)
    CSLGHN.set("JLLNCD",  mi.in.get("LNCD"))
    if (!query_CSLGHN.read(CSLGHN)) {
      CSLGHN.set("JLTX60", iDescription)
      CSLGHN.set("JLTX15", iShortDescription)
      CSLGHN.set("JLFILE", mi.in.get("FILE"))
      CSLGHN.set("JLFLDI", mi.in.get("FLDI"))
      CSLGHN.setInt("JLRGDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
      CSLGHN.setInt("JLRGTM", timeOfCreation.format(DateTimeFormatter.ofPattern("HHmmss")) as Integer)
      CSLGHN.setInt("JLLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
      CSLGHN.setInt("JLCHNO", 1)
      CSLGHN.set("JLCHID", program.getUser())
      query_CSLGHN.insert(CSLGHN)
    } else {
      mi.error("L'enregistrement existe déjà")
    }

    if(mi.in.get("FILE")=="CSEAMA"){
      // Update CSEAMA DTID
      DBAction query_CSEAMA = database.table("CSEAMA").index("00").build()
      DBContainer CSEAMA = query_CSEAMA.getContainer()
      CSEAMA.set("HSCONO", currentCompany)
      CSEAMA.set("HSSEA1", mi.in.get("STKY"))
      if (!query_CSEAMA.readLock(CSEAMA, updateCallBack_CSEAMA)) {
        mi.error("L'enregistrement n'existe pas")
      }
    }
    // Update CSYTAB TXID
    if(mi.in.get("FILE")=="CSYTAB"){
      DBAction query_CSYTAB = database.table("CSYTAB").index("00").build()
      DBContainer CSYTAB = query_CSYTAB.getContainer()
      CSYTAB.set("CTCONO", currentCompany)
      CSYTAB.set("CTSTCO", "ITGR")
      CSYTAB.set("CTSTKY", mi.in.get("STKY"))
      if (!query_CSYTAB.readLock(CSYTAB, updateCallBack_CSYTAB)) {
        mi.error("L'enregistrement n'existe pas")
      }
    }
    mi.outData.put("DTID", dtid as String)
    mi.write()
  }
  // Retrieve CSLGHN
  Closure<?> outData_CSLGHN = { DBContainer CSLGHN_0 ->
    dtid = CSLGHN_0.get("JLDTID")
    dtid++
  }
  // Update CSYNUM
  Closure<?> updateCallBack = { LockedResult lockedResult ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    int changeNumber = lockedResult.get("CMCHNO")
    lockedResult.set("CMNBNR", dtid)
    lockedResult.setInt("CMLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
    lockedResult.setInt("CMCHNO", changeNumber + 1)
    lockedResult.set("CMCHID", program.getUser())
    lockedResult.update()
  }
  // Update CSEAMA
  Closure<?> updateCallBack_CSEAMA = { LockedResult lockedResult ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    int changeNumber = lockedResult.get("HSCHNO")
    lockedResult.set("HSDTID", dtid)
    lockedResult.setInt("HSLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
    lockedResult.setInt("HSCHNO", changeNumber + 1)
    lockedResult.set("HSCHID", program.getUser())
    lockedResult.update()
  }
  // Update CSYTAB
  Closure<?> updateCallBack_CSYTAB = { LockedResult lockedResult ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    int changeNumber = lockedResult.get("CTCHNO")
    lockedResult.set("CTTXID", dtid)
    lockedResult.setInt("CTLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
    lockedResult.setInt("CTCHNO", changeNumber + 1)
    lockedResult.set("CTCHID", program.getUser())
    lockedResult.update()
  }
}

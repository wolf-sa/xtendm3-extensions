/**
 * README
 * This extension is used by scheduler
 *
 * Name : EXT010
 * Description : batch template
 * Date         Changed By   Description
 * 20221109     RENARN       LOGX01 - Preallocate
 * 20221229     RENARN       Added order category 030
 * 20230103     RENARN       Change of sorting order
 * 20230602     RENARN       Added : drdx, ardx parameters for MWS121MI.Preallocate
 */
import java.time.LocalTime
import java.time.Duration
import java.time.format.DateTimeFormatter
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

public class EXT010 extends ExtendM3Batch {
    private final LoggerAPI logger
    private final DatabaseAPI database
    private final ProgramAPI program
    private final BatchAPI batch
    private final MICallerAPI miCaller
    private final TextFilesAPI textFiles
    private final UtilityAPI utility
    private Integer currentCompany
    private String rawData
    private int rawDataLength
    private int beginIndex
    private int endIndex
    private String logFileName
    private boolean IN60
    private String jobNumber
    private String currentDate
    private String doca
    private String drdn
    private String drdl
    private String drdx
    private String aoca
    private String ardn
    private String ardl
    private String ardx
    private String pqty
    private String orno
    private Integer ponr
    private Integer posx
    private String acquisitionOrderCategory
    private String acquisitionOrder
    private Integer acquisitionOrderLine
    private Integer acquisitionOrderLineSuffix
    private Double acquisitionQuantity
    private String acquisitionPlanningDate
    private String acquisitionWarehouse
    private String acquisitionItemNumber
    private String demandOrderCategory
    private String demandOrder
    private Integer demandOrderLine
    private Integer demandOrderLineSuffix
    private Double demandQuantity
    public boolean statusLowerThan33
    public double allocatedQuantity
    public double preallocateQuantity
    public double alreadyPreallocatedQuantity
    private LocalTime jobStartTime
    private LocalTime actualTime
    private boolean timeOut_HasBeenExceeded = false
    private Integer timeOutInMinutes
    private String inJobNumber
    private String itno
    private String iWHLO
    private String iITNO

    public EXT010(LoggerAPI logger, DatabaseAPI database, ProgramAPI program, BatchAPI batch, MICallerAPI miCaller, TextFilesAPI textFiles, UtilityAPI utility) {
        this.logger = logger
        this.database = database
        this.program = program
        this.batch = batch
        this.miCaller = miCaller
        this.textFiles = textFiles
        this.utility = utility
    }

    public void main() {
        // Get job number
        LocalDateTime timeOfCreation = LocalDateTime.now()
        jobNumber = program.getJobNumber() + timeOfCreation.format(DateTimeFormatter.ofPattern("yyMMdd")) + timeOfCreation.format(DateTimeFormatter.ofPattern("HHmmss"))

        //logger.info("Début" + program.getProgramName())
        if (batch.getReferenceId().isPresent()) {
            Optional<String> data = getJobData(batch.getReferenceId().get())
            //logger.info("data = " + data)
            performActualJob(data)
        } else {
            // No job data found
            //logger.info("Job data for job ${batch.getJobId()} is missing")
        }
    }
    // Get job data
    private Optional<String> getJobData(String referenceId) {
        def query = database.table("EXTJOB").index("00").selection("EXDATA").build()
        def container = query.createContainer()
        container.set("EXRFID", referenceId)
        if (query.read(container)) {
            //logger.info("EXDATA = " + container.getString("EXDATA"))
            return Optional.of(container.getString("EXDATA"))
        } else {
            //logger.info("EXTJOB not found")
        }
        return Optional.empty()
    }
    // Perform actual job
    private performActualJob(Optional<String> data) {
        if (!data.isPresent()) {
            //logger.info("Job reference Id ${batch.getReferenceId().get()} is passed but data was not found")
            return
        }
        rawData = data.get()
        //logger.info("Début performActualJob")
        inJobNumber = getFirstParameter()

        logger.info("jobNumber 1 = " + jobNumber)
        if (inJobNumber != "" && inJobNumber != null) {
            jobNumber = inJobNumber
            logger.info("jobNumber 2 = " + jobNumber)
        }

        currentCompany = (Integer) program.getLDAZD().CONO
        //logger.info("currentCompany" + currentCompany)

        LocalDateTime timeOfCreation = LocalDateTime.now()
        currentDate = timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer

        // Init time out
        timeOutInMinutes = 59

        String currentTime = timeOfCreation.format(DateTimeFormatter.ofPattern("HHmmss"))
        jobStartTime = LocalTime.parse(currentTime, DateTimeFormatter.ofPattern("HHmmss"))

        // Perform Job

        // Init work file
        initWorkFile()

        // Process work file
        processWorkFile()

        if (timeOut_HasBeenExceeded) {
            // Resubmit batch for remaining records in EXT010 work file
            executeEXT820MISubmitBatch(currentCompany as String,"EXT010", jobNumber)
            logger.info("Last processed item - iITNO = " + iITNO)
            logger.info("Time out " + timeOutInMinutes + " minutes has been reached - End of job")
            logger.info("jobStartTime = " + jobStartTime)
            logger.info("actualTime = " + actualTime)
        }

        // Delete file EXTJOB
        //deleteEXTJOB()
    }
    // Init work file
    public void initWorkFile() {
        if(inJobNumber == "" || inJobNumber == null) {
            DBAction query = database.table("MITPLO").index("90").selection("MOWHLO", "MOITNO").build()
            DBContainer MITPLO90 = query.getContainer()
            MITPLO90.set("MOCONO", currentCompany)
            MITPLO90.set("MOORCA", "251")
            if (!query.readAll(MITPLO90, 2, outData_MITPLO90init)) {
            }
            // Reading of the acquisition material plan (DO)
            //logger.info("Début preallocation 501");
            MITPLO90.set("MOCONO", currentCompany)
            MITPLO90.set("MOORCA", "501")
            if (!query.readAll(MITPLO90, 2, outData_MITPLO90init)) {
            }
        }
    }
    // Process work file
    public void processWorkFile() {
        DBAction query = database.table("EXT010").index("00").selection("EXWHLO", "EXITNO").build()
        DBContainer EXT010 = query.getContainer()
        EXT010.set("EXBJNO", jobNumber)
        if (!query.readAll(EXT010, 1, outData_EXT010)) {
        }
    }
    // Removal of existing pre-allocations
    public void preallocationRemoval() {
        // Read pre-allocations
        DBAction query = database.table("MPREAL").index("60").selection("NADOCA", "NADRDN", "NADRDL", "NADRDX", "NAAOCA", "NAARDN", "NAARDL", "NAARDX", "NAPQTY").build()
        DBContainer MPREAL = query.getContainer()
        MPREAL.set("NACONO", currentCompany)
        MPREAL.set("NAWHLO", iWHLO)
        MPREAL.set("NAITNO", iITNO)
        if (!query.readAll(MPREAL, 3, outData_MPREAL)) {
        }
    }
    // Retrieve EXT010
    Closure<?> outData_EXT010 = { DBContainer EXT010 ->
        if (timeOutExceeded())
            return false

        if(iITNO == "" || iITNO == null) {
            logger.info("First processed item = " + EXT010.get("EXITNO"))
        }

        iWHLO = EXT010.get("EXWHLO")
        iITNO = EXT010.get("EXITNO")

        // Removal of existing pre-allocations
        preallocationRemoval()

        // Preallocation
        preallocation()

        // Delete processed record
        deleteProcessedRecord()
    }
    // Delete record
    Closure<?> updateCallBack_EXT010 = { LockedResult lockedResult ->
        lockedResult.delete()
    }
    // Delete EXT010 record
    public void deleteProcessedRecord() {
        DBAction EXT010_query = database.table("EXT010").index("00").selection("EXWHLO", "EXITNO").build()
        DBContainer EXT010 = EXT010_query.getContainer()
        EXT010.set("EXBJNO", jobNumber);
        EXT010.set("EXWHLO", iWHLO);
        EXT010.set("EXITNO", iITNO);
        if (!EXT010_query.readLock(EXT010, updateCallBack_EXT010)) {
        }
    }
    // Retrieve MPREAL
    Closure<?> outData_MPREAL = { DBContainer MPREAL ->
        // Remove pre-allocations
        //logger.info("Found MPREAL")
        // Remove pre-allocations
        doca = MPREAL.get("NADOCA")
        drdn = MPREAL.get("NADRDN")
        drdl = MPREAL.get("NADRDL")
        drdx = MPREAL.get("NADRDX")
        aoca = MPREAL.get("NAAOCA")
        ardn = MPREAL.get("NAARDN")
        ardl = MPREAL.get("NAARDL")
        ardx = MPREAL.get("NAARDX")
        pqty = "0"
        //logger.info("executeMWS121MIPreallocate : " + doca + "/" + drdn + "/" + drdl + "/" + aoca + "/" + ardn + "/" + ardl + "/" + pqty + "/")
        executeMWS121MIPreallocate(doca, drdn, drdl, drdx, aoca, ardn, ardl, ardx, pqty)
    }
    // Retrieve MPREAL
    Closure<?> outData_MPREAL2 = { DBContainer MPREAL ->
        alreadyPreallocatedQuantity = MPREAL.get("NAPQTY")
        demandQuantity = demandQuantity - alreadyPreallocatedQuantity
        //logger.info("alreadyPreallocatedQuantity = " + alreadyPreallocatedQuantity)
        //logger.info("demandQuantity = " + demandQuantity)
    }
    // Preallocation
    public void preallocation() {
        // Reading of the acquisition material plan (PO)
        //logger.info("Début preallocation 251");
        DBAction query = database.table("MITPLO").index("20").selection("MOORCA", "MORIDN", "MORIDL", "MORIDX", "MOTRQT", "MOPLDT", "MOWHLO", "MOITNO").build()
        DBContainer MITPLO20 = query.getContainer()
        MITPLO20.set("MOCONO", currentCompany)
        MITPLO20.set("MOWHLO", iWHLO)
        MITPLO20.set("MOITNO", iITNO)
        MITPLO20.set("MOORCA", "251")
        if (!query.readAll(MITPLO20, 4, outData_MITPLO20)) {
        }
        // Reading of the acquisition material plan (DO)
        //logger.info("Début preallocation 501");
        MITPLO20.set("MOCONO", currentCompany)
        MITPLO20.set("MOWHLO", iWHLO)
        MITPLO20.set("MOITNO", iITNO)
        MITPLO20.set("MOORCA", "501")
        if (!query.readAll(MITPLO20, 4, outData_MITPLO20)) {
        }
    }
    // Retrieve MITPLO90
    Closure<?> outData_MITPLO90init = { DBContainer MITPLO90 ->
        LocalDateTime timeOfCreation = LocalDateTime.now()
        DBAction query_EXT010 = database.table("EXT010").index("00").build()
        DBContainer EXT010 = query_EXT010.getContainer()
        EXT010.set("EXBJNO", jobNumber)
        EXT010.set("EXWHLO", MITPLO90.get("MOWHLO"))
        EXT010.set("EXITNO", MITPLO90.get("MOITNO"))
        if (!query_EXT010.read(EXT010)) {
            EXT010.setInt("EXRGDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
            EXT010.setInt("EXRGTM", timeOfCreation.format(DateTimeFormatter.ofPattern("HHmmss")) as Integer)
            EXT010.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
            EXT010.setInt("EXCHNO", 1)
            EXT010.set("EXCHID", program.getUser())
            query_EXT010.insert(EXT010)
            return false
        }
    }
    // Retrieve MITPLO20
    Closure<?> outData_MITPLO20 = { DBContainer MITPLO20 ->
        acquisitionOrderCategory = MITPLO20.get("MOORCA")
        acquisitionOrder = MITPLO20.get("MORIDN")
        acquisitionOrderLine = MITPLO20.get("MORIDL")
        acquisitionOrderLineSuffix = MITPLO20.get("MORIDX")
        acquisitionQuantity = MITPLO20.get("MOTRQT")
        acquisitionPlanningDate = MITPLO20.get("MOPLDT")
        acquisitionWarehouse = MITPLO20.get("MOWHLO")
        acquisitionItemNumber = MITPLO20.get("MOITNO")

        //logger.info("acquisitionOrderCategory = " + acquisitionOrderCategory)
        //logger.info("acquisitionOrder = " + acquisitionOrder)
        //logger.info("acquisitionOrderLine = " + acquisitionOrderLine)
        //logger.info("acquisitionQuantity = " + acquisitionQuantity)
        //logger.info("acquisitionPlanningDate = " + acquisitionPlanningDate)
        //logger.info("acquisitionWarehouse = " + acquisitionWarehouse)
        //logger.info("acquisitionItemNumber = " + acquisitionItemNumber)

        // Reading of the demand material plan (Customer order)
        ExpressionFactory expression_MITPLO = database.getExpressionFactory("MITPLO")
        expression_MITPLO = expression_MITPLO.ge("MOPLDT", acquisitionPlanningDate)
        expression_MITPLO = expression_MITPLO.and((expression_MITPLO.eq("MOORCA", "311")).or(expression_MITPLO.eq("MOORCA", "511")).or(expression_MITPLO.eq("MOORCA", "030")))
        DBAction query = database.table("MITPLO").index("00").matching(expression_MITPLO).selection("MOORCA", "MORIDN", "MORIDL", "MORIDX", "MOTRQT", "MOPLDT").build()
        DBContainer MITPLO00 = query.getContainer()
        MITPLO00.set("MOCONO", currentCompany)
        MITPLO00.set("MOWHLO", acquisitionWarehouse)
        MITPLO00.set("MOITNO", acquisitionItemNumber)
        if (!query.readAll(MITPLO00, 3, outData_MITPLO00)) {
        }
    }
// Retrieve MITPLO00
    Closure<?> outData_MITPLO00 = { DBContainer MITPLO00 ->
        demandOrderCategory = MITPLO00.get("MOORCA")
        demandOrder = MITPLO00.get("MORIDN")
        demandOrderLine = MITPLO00.get("MORIDL")
        demandOrderLineSuffix = MITPLO00.get("MORIDX")
        demandQuantity = MITPLO00.get("MOTRQT")
        demandQuantity = demandQuantity * -1

        //logger.info("demandOrderCategory = " + demandOrderCategory)
        //logger.info("demandOrder = " + demandOrder)
        //logger.info("demandOrderLine = " + demandOrderLine)
        //logger.info("demandOrderLineSuffix = " + demandOrderLineSuffix)
        //logger.info("demandQuantity = " + demandQuantity)

        if (demandOrderCategory == "311") {
            statusLowerThan33 = false
            ExpressionFactory expression_OOLINE = database.getExpressionFactory("OOLINE")
            expression_OOLINE = expression_OOLINE.lt("OBORST", "33")
            DBAction OOLINE_query = database.table("OOLINE").index("00").matching(expression_OOLINE).selection("OBORNO", "OBPONR", "OBPOSX", "OBORST").build()
            DBContainer OOLINE = OOLINE_query.getContainer()
            OOLINE.set("OBCONO", currentCompany)
            OOLINE.set("OBORNO", demandOrder)
            OOLINE.set("OBPONR", demandOrderLine)
            OOLINE.set("OBPOSX", demandOrderLineSuffix)
            if (OOLINE_query.read(OOLINE)) {
                statusLowerThan33 = true
            }
            //logger.info("statusLowerThan33 = " + statusLowerThan33)
            if (statusLowerThan33) {
                // Deduction of the quantity already preallocated
                DBAction query = database.table("MPREAL").index("10").selection("NADOCA", "NADRDN", "NADRDL", "NAAOCA", "NAARDN", "NAARDL", "NAPQTY").build()
                DBContainer MPREAL = query.getContainer()
                MPREAL.set("NACONO", currentCompany)
                MPREAL.set("NADOCA", demandOrderCategory)
                MPREAL.set("NADRDN", demandOrder)
                MPREAL.set("NADRDL", demandOrderLine)
                MPREAL.set("NADRDX", demandOrderLineSuffix)
                if (!query.readAll(MPREAL, 5, outData_MPREAL2)) {
                }
                // Deduction of the quantity already allocated
                DBAction query_MITALO = database.table("MITALO").index("10").selection("MQBANO", "MQALQT").build()
                DBContainer MITALO = query_MITALO.getContainer()
                MITALO.set("MQCONO", currentCompany)
                MITALO.set("MQTTYP", 31)
                MITALO.set("MQRIDN", demandOrder)
                MITALO.set("MQRIDO", 0)
                MITALO.set("MQRIDL", demandOrderLine)
                MITALO.set("MQRIDX", demandOrderLineSuffix)
                if (query_MITALO.readAll(MITALO, 6, outData_MITALO)) {
                }
            }
        }
        if (demandOrderCategory == "511") {
            statusLowerThan33 = false
            ExpressionFactory expression_MGLINE = database.getExpressionFactory("MGLINE")
            expression_MGLINE = expression_MGLINE.lt("MRTRSH", "33")
            DBAction MGLINE_query = database.table("MGLINE").index("00").matching(expression_MGLINE).selection("MRTRNR", "MRPONR", "MRPOSX", "MRTRSH").build()
            DBContainer MGLINE = MGLINE_query.getContainer()
            MGLINE.set("MRCONO", currentCompany)
            MGLINE.set("MRTRNR", demandOrder)
            MGLINE.set("MRPONR", demandOrderLine)
            MGLINE.set("MRPOSX", demandOrderLineSuffix)
            if (MGLINE_query.read(MGLINE)) {
                statusLowerThan33 = true
            }
            //logger.info("statusLowerThan33 = " + statusLowerThan33)
            if (statusLowerThan33) {
                // Deduction of the quantity already preallocated
                DBAction query = database.table("MPREAL").index("10").selection("NADOCA", "NADRDN", "NADRDL", "NAAOCA", "NAARDN", "NAARDL", "NAPQTY").build()
                DBContainer MPREAL = query.getContainer()
                MPREAL.set("NACONO", currentCompany)
                MPREAL.set("NADOCA", demandOrderCategory)
                MPREAL.set("NADRDN", demandOrder)
                MPREAL.set("NADRDL", demandOrderLine)
                MPREAL.set("NADRDX", demandOrderLineSuffix)
                if (!query.readAll(MPREAL, 5, outData_MPREAL2)) {
                }
                // Deduction of the quantity already allocated
                DBAction query_MITALO = database.table("MITALO").index("10").selection("MQBANO", "MQALQT").build()
                DBContainer MITALO = query_MITALO.getContainer()
                MITALO.set("MQCONO", currentCompany)
                MITALO.set("MQTTYP", 51)
                MITALO.set("MQRIDN", demandOrder)
                MITALO.set("MQRIDO", 0)
                MITALO.set("MQRIDL", demandOrderLine)
                MITALO.set("MQRIDX", demandOrderLineSuffix)
                if (query_MITALO.readAll(MITALO, 6, outData_MITALO)) {
                }
            }
        }
        if (demandOrderCategory == "030") {
            //logger.info("demandOrderCategory == 030")
            // Deduction of the quantity already preallocated
            DBAction query = database.table("MPREAL").index("10").selection("NADOCA", "NADRDN", "NADRDL", "NAAOCA", "NAARDN", "NAARDL", "NAPQTY").build()
            DBContainer MPREAL = query.getContainer()
            MPREAL.set("NACONO", currentCompany)
            MPREAL.set("NADOCA", demandOrderCategory)
            MPREAL.set("NADRDN", demandOrder)
            MPREAL.set("NADRDL", demandOrderLine)
            MPREAL.set("NADRDX", demandOrderLineSuffix)
            if (!query.readAll(MPREAL, 5, outData_MPREAL2)) {
            }
            // Deduction of the quantity already allocated
            DBAction query_MITALO = database.table("MITALO").index("10").selection("MQBANO", "MQALQT").build()
            DBContainer MITALO = query_MITALO.getContainer()
            MITALO.set("MQCONO", currentCompany)
            MITALO.set("MQTTYP", 3)
            MITALO.set("MQRIDN", demandOrder)
            MITALO.set("MQRIDO", 0)
            MITALO.set("MQRIDL", demandOrderLine)
            MITALO.set("MQRIDX", demandOrderLineSuffix)
            if (query_MITALO.readAll(MITALO, 6, outData_MITALO)) {
            }
        }
        if (statusLowerThan33 || demandOrderCategory == "030") {
            //logger.info("Before calculation----------------------------")
            //logger.info("acquisitionQuantity = " + acquisitionQuantity)
            //logger.info("demandQuantity = " + demandQuantity)
            // Calculation of the quantity to pre-allocate
            preallocateQuantity = 0
            if (acquisitionQuantity > 0 && demandQuantity > 0) {
                if (acquisitionQuantity >= demandQuantity) {
                    preallocateQuantity = demandQuantity
                    acquisitionQuantity = acquisitionQuantity - preallocateQuantity
                } else {
                    preallocateQuantity = acquisitionQuantity
                    acquisitionQuantity = acquisitionQuantity - preallocateQuantity
                }
            }
            //logger.info("After calculation----------------------------")
            //logger.info("preallocateQuantity = " + preallocateQuantity)
            //logger.info("acquisitionQuantity = " + acquisitionQuantity)
            if (preallocateQuantity > 0) {
                //logger.info("execute MWS121MI.Preallocate")
                // execute MWS121MI.Preallocate
                doca = demandOrderCategory
                drdn = demandOrder
                drdl = demandOrderLine
                drdx = demandOrderLineSuffix
                aoca = acquisitionOrderCategory
                ardn = acquisitionOrder
                ardl = acquisitionOrderLine
                ardx = acquisitionOrderLineSuffix
                pqty = preallocateQuantity
                //logger.info("executeMWS121MIPreallocate : " + aoca + "/" + ardn + "/" + ardl + "/" + doca + "/" + drdn + "/" + drdl + "/" + pqty)
                executeMWS121MIPreallocate(doca, drdn, drdl, drdx, aoca, ardn, ardl, ardx, pqty)
            }
        }
    }
    // Retrieve MITALO
    Closure<?> outData_MITALO = { DBContainer MITALO ->
        allocatedQuantity = MITALO.get("MQALQT")
        demandQuantity = demandQuantity - allocatedQuantity
        //logger.info("allocatedQuantity = " + allocatedQuantity)
        //logger.info("demandQuantity = " + demandQuantity)
    }
    // Execute MWS121MI.Preallocate
    private executeMWS121MIPreallocate(String DOCA, String DRDN, String DRDL, String DRDX, String AOCA, String ARDN, String ARDL, String ARDX, String PQTY) {
        def parameters = ["DOCA": DOCA, "DRDN": DRDN, "DRDL": DRDL, "DRDX": DRDX, "AOCA": AOCA, "ARDN": ARDN, "ARDL": ARDL, "ARDX": ARDX, "PQTY": PQTY]
        Closure<?> handler = { Map<String, String> response ->
            if (response.error != null) {
            } else {
            }
        }
        miCaller.call("MWS121MI", "Preallocate", parameters, handler)
    }
    // Get first parameter
    private String getFirstParameter() {
        //logger.info("rawData = " + rawData)
        rawDataLength = rawData.length()
        beginIndex = 0
        endIndex = rawData.indexOf(";")
        // Get parameter
        String parameter = rawData.substring(beginIndex, endIndex)
        //logger.info("parameter = " + parameter)
        return parameter
    }
    // Get next parameter
    private String getNextParameter() {
        beginIndex = endIndex + 1
        endIndex = rawDataLength - rawData.indexOf(";") - 1
        rawData = rawData.substring(beginIndex, rawDataLength)
        rawDataLength = rawData.length()
        beginIndex = 0
        endIndex = rawData.indexOf(";")
        // Get parameter
        String parameter = rawData.substring(beginIndex, endIndex)
        //logger.info("parameter = " + parameter)
        return parameter
    }
    // Delete records related to the current job from EXTJOB table
    //public void deleteEXTJOB(){
    //    LocalDateTime timeOfCreation = LocalDateTime.now()
    //  DBAction query = database.table("EXTJOB").index("00").build()
    //  DBContainer EXTJOB = query.getContainer()
    //  EXTJOB.set("EXRFID", batch.getReferenceId().get())
    //  if(!query.readAllLock(EXTJOB, 1, updateCallBack_EXTJOB)){
    //  }
    //}
    // Delete EXTJOB
    Closure<?> updateCallBack_EXTJOB = { LockedResult lockedResult ->
        lockedResult.delete()
    }
    // Log
    void log(String message) {
        IN60 = true
        //logger.info(message)
        message = LocalDateTime.now().toString() + ";" + message
        Closure<?> consumer = { PrintWriter printWriter ->
            printWriter.println(message)
        }
        textFiles.write(logFileName, "UTF-8", true, consumer)
    }
    private boolean timeOutExceeded() {
        LocalDateTime timeActual = LocalDateTime.now()
        String currentTime2 = timeActual.format(DateTimeFormatter.ofPattern("HHmmss"))
        actualTime = LocalTime.parse(currentTime2, DateTimeFormatter.ofPattern("HHmmss"))

        Duration difference = Duration.between(jobStartTime, actualTime)
        long differenceInMinutes = difference.toMinutes()
        if (differenceInMinutes >= timeOutInMinutes) {
            timeOut_HasBeenExceeded = true
            return true
        }
        return false
    }
    /**
     * Submit job
     */
    private executeEXT820MISubmitBatch(String CONO, String JOID, String P001){
        def parameters = ["CONO": CONO, "JOID": JOID, "P001": P001]
        Closure<?> handler = { Map<String, String> response ->
            if (response.error != null) {
                return logger.info("Failed EXT820MI.SubmitBatch: "+ response.errorMessage)
            } else {
            }
        }
        miCaller.call("EXT820MI", "SubmitBatch", parameters, handler)
    }
}
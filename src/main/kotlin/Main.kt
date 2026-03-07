import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class Student(val name: String, val score: Double) {
    val grade: String
        get() = when {
            score >= 90 -> "A"
            score >= 80 -> "B"
            score >= 70 -> "C"
            score >= 60 -> "D"
            else        -> "F"
        }
    val status: String
        get() = if (score >= 60) "PASS" else "FAIL"
}

fun main() {
    val students = mutableListOf<Student>()
    printBanner()
    var running = true
    while (running) {
        printMenu()
        when (readLine()?.trim()) {
            "1" -> addStudent(students)
            "2" -> viewStudents(students)
            "3" -> exportToExcel(students)
            "4" -> {
                println("\nGoodbye! Excel report saved to project folder.\n")
                running = false
            }
            else -> println("\nInvalid option. Please choose 1-4.\n")
        }
    }
}

fun printBanner() {
    println("================================================")
    println("       STUDENT GRADING CALCULATOR v1.0          ")
    println("================================================")
    println("  Grade Scale:                                  ")
    println("    90 - 100  =  A  (Excellent)                 ")
    println("    80 -  89  =  B  (Good)                      ")
    println("    70 -  79  =  C  (Average)                   ")
    println("    60 -  69  =  D  (Below Average)             ")
    println("     0 -  59  =  F  (Fail)                      ")
    println("================================================\n")
}

fun printMenu() {
    println("------------------------------------------------")
    println("                  MAIN MENU                     ")
    println("------------------------------------------------")
    println("  1. Add Student")
    println("  2. View All Students")
    println("  3. Export to Excel (.xlsx)")
    println("  4. Exit")
    println("------------------------------------------------")
    print("Enter choice: ")
}

fun addStudent(students: MutableList<Student>) {
    println("\n--- Add Student ---")
    print("Student Name : ")
    val name = readLine()?.trim()
    if (name.isNullOrEmpty()) {
        println("Name cannot be empty.\n")
        return
    }
    print("Score (0-100): ")
    val score = readLine()?.trim()?.toDoubleOrNull()
    if (score == null || score < 0 || score > 100) {
        println("Invalid score. Enter a number between 0 and 100.\n")
        return
    }
    val student = Student(name, score)
    students.add(student)
    println("\nStudent Added!")
    println("  Name   : ${student.name}")
    println("  Score  : ${student.score}")
    println("  Grade  : ${student.grade}")
    println("  Status : ${student.status}\n")
}

fun viewStudents(students: List<Student>) {
    println("\n--- Student Records ---")
    if (students.isEmpty()) {
        println("No students added yet. Use option 1 to add students.\n")
        return
    }
    println("%-5s %-25s %-10s %-8s %-8s".format("#", "Name", "Score", "Grade", "Status"))
    println("-".repeat(60))
    students.forEachIndexed { i, s ->
        println("%-5d %-25s %-10.1f %-8s %-8s".format(i + 1, s.name, s.score, s.grade, s.status))
    }
    println("-".repeat(60))
    println("Total Students : ${students.size}")
    println("Class Average  : ${"%.2f".format(students.map { it.score }.average())}")
    println("Highest Score  : ${students.maxByOrNull { it.score }?.name} (${students.maxByOrNull { it.score }?.score})")
    println("Lowest Score   : ${students.minByOrNull { it.score }?.name} (${students.minByOrNull { it.score }?.score})\n")
}

fun exportToExcel(students: List<Student>) {
    if (students.isEmpty()) {
        println("\nNo students to export. Add students first.\n")
        return
    }

    val wb = XSSFWorkbook()
    val sheet = wb.createSheet("Grade Report")

    // --- Styles ---
    val titleStyle = wb.createCellStyle().apply {
        setFont(wb.createFont().apply {
            bold = true; fontHeightInPoints = 16; fontName = "Arial"
            color = IndexedColors.WHITE.index
        })
        fillForegroundColor = IndexedColors.DARK_BLUE.index
        fillPattern = FillPatternType.SOLID_FOREGROUND
        alignment = HorizontalAlignment.CENTER
        verticalAlignment = VerticalAlignment.CENTER
    }

    val headerStyle = wb.createCellStyle().apply {
        setFont(wb.createFont().apply {
            bold = true; fontHeightInPoints = 11; fontName = "Arial"
            color = IndexedColors.WHITE.index
        })
        fillForegroundColor = IndexedColors.GREY_50_PERCENT.index
        fillPattern = FillPatternType.SOLID_FOREGROUND
        alignment = HorizontalAlignment.CENTER
        borderTop = BorderStyle.THIN; borderBottom = BorderStyle.THIN
        borderLeft = BorderStyle.THIN; borderRight = BorderStyle.THIN
    }

    val dataStyle = wb.createCellStyle().apply {
        alignment = HorizontalAlignment.CENTER
        borderTop = BorderStyle.THIN; borderBottom = BorderStyle.THIN
        borderLeft = BorderStyle.THIN; borderRight = BorderStyle.THIN
        setFont(wb.createFont().apply { fontName = "Arial"; fontHeightInPoints = 11 })
    }

    val numberStyle = wb.createCellStyle().apply {
        alignment = HorizontalAlignment.CENTER
        borderTop = BorderStyle.THIN; borderBottom = BorderStyle.THIN
        borderLeft = BorderStyle.THIN; borderRight = BorderStyle.THIN
        dataFormat = wb.createDataFormat().getFormat("0.0")
        setFont(wb.createFont().apply { fontName = "Arial"; fontHeightInPoints = 11 })
    }

    fun gradeStyle(grade: String): CellStyle {
        val color = when (grade) {
            "A"  -> IndexedColors.LIGHT_GREEN.index
            "B"  -> IndexedColors.LIGHT_TURQUOISE.index
            "C"  -> IndexedColors.LIGHT_YELLOW.index
            "D"  -> IndexedColors.LIGHT_ORANGE.index
            else -> IndexedColors.ROSE.index
        }
        return wb.createCellStyle().apply {
            fillForegroundColor = color
            fillPattern = FillPatternType.SOLID_FOREGROUND
            alignment = HorizontalAlignment.CENTER
            borderTop = BorderStyle.THIN; borderBottom = BorderStyle.THIN
            borderLeft = BorderStyle.THIN; borderRight = BorderStyle.THIN
            setFont(wb.createFont().apply {
                bold = true; fontName = "Arial"; fontHeightInPoints = 11
            })
        }
    }

    fun statusStyle(status: String): CellStyle {
        val color = if (status == "PASS") IndexedColors.LIGHT_GREEN.index else IndexedColors.ROSE.index
        return wb.createCellStyle().apply {
            fillForegroundColor = color
            fillPattern = FillPatternType.SOLID_FOREGROUND
            alignment = HorizontalAlignment.CENTER
            borderTop = BorderStyle.THIN; borderBottom = BorderStyle.THIN
            borderLeft = BorderStyle.THIN; borderRight = BorderStyle.THIN
            setFont(wb.createFont().apply { bold = true; fontName = "Arial"; fontHeightInPoints = 11 })
        }
    }

    // --- Title ---
    sheet.addMergedRegion(org.apache.poi.ss.util.CellRangeAddress(0, 1, 0, 4))
    val titleRow = sheet.createRow(0)
    titleRow.height = 800
    titleRow.createCell(0).apply {
        setCellValue("Student Grade Report")
        cellStyle = titleStyle
    }

    // --- Date ---
    val now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
    sheet.createRow(2).createCell(0).setCellValue("Generated: $now")

    // --- Headers ---
    val headerRow = sheet.createRow(4)
    listOf("#", "Student Name", "Score", "Grade", "Status").forEachIndexed { i, title ->
        headerRow.createCell(i).apply {
            setCellValue(title)
            cellStyle = headerStyle
        }
    }

    // --- Student Data ---
    students.forEachIndexed { idx, s ->
        val row = sheet.createRow(5 + idx)
        row.createCell(0).apply { setCellValue((idx + 1).toDouble()); cellStyle = dataStyle }
        row.createCell(1).apply { setCellValue(s.name);               cellStyle = dataStyle }
        row.createCell(2).apply { setCellValue(s.score);              cellStyle = numberStyle }
        row.createCell(3).apply { setCellValue(s.grade);              cellStyle = gradeStyle(s.grade) }
        row.createCell(4).apply { setCellValue(s.status);             cellStyle = statusStyle(s.status) }
    }

    // --- Summary ---
    val sumRow = 7 + students.size
    val range  = "C6:C${5 + students.size}"
    val labels = listOf("Total Students", "Class Average", "Highest Score", "Lowest Score", "Pass Count", "Fail Count")
    val values = listOf(
        students.size.toString(),
        "=AVERAGE($range)",
        "=MAX($range)",
        "=MIN($range)",
        students.count { it.status == "PASS" }.toString(),
        students.count { it.status == "FAIL" }.toString()
    )
    val summaryLabelStyle = wb.createCellStyle().apply {
        setFont(wb.createFont().apply { bold = true; fontName = "Arial"; fontHeightInPoints = 11 })
        fillForegroundColor = IndexedColors.PALE_BLUE.index
        fillPattern = FillPatternType.SOLID_FOREGROUND
        borderTop = BorderStyle.THIN; borderBottom = BorderStyle.THIN
        borderLeft = BorderStyle.THIN; borderRight = BorderStyle.THIN
        alignment = HorizontalAlignment.RIGHT
    }
    val summaryValueStyle = wb.createCellStyle().apply {
        setFont(wb.createFont().apply { fontName = "Arial"; fontHeightInPoints = 11 })
        borderTop = BorderStyle.THIN; borderBottom = BorderStyle.THIN
        borderLeft = BorderStyle.THIN; borderRight = BorderStyle.THIN
        alignment = HorizontalAlignment.CENTER
    }
    labels.forEachIndexed { i, label ->
        val row = sheet.createRow(sumRow + i)
        row.createCell(2).apply { setCellValue(label); cellStyle = summaryLabelStyle }
        val vc = row.createCell(3)
        if (values[i].startsWith("=")) vc.cellFormula = values[i].drop(1)
        else vc.setCellValue(values[i].toDoubleOrNull() ?: 0.0)
        vc.cellStyle = summaryValueStyle
    }

    // --- Grade Legend ---
    val legendRow = sumRow + labels.size + 2
    sheet.createRow(legendRow).createCell(0).apply {
        setCellValue("Grade Scale")
        cellStyle = wb.createCellStyle().apply {
            setFont(wb.createFont().apply { bold = true; fontName = "Arial"; fontHeightInPoints = 11 })
        }
    }
    listOf("A" to "90-100", "B" to "80-89", "C" to "70-79", "D" to "60-69", "F" to "0-59")
        .forEachIndexed { i, (g, r) ->
            val row = sheet.createRow(legendRow + 1 + i)
            row.createCell(0).apply { setCellValue(g); cellStyle = gradeStyle(g) }
            row.createCell(1).apply { setCellValue(r); cellStyle = dataStyle }
        }

    // --- Column Widths ---
    sheet.setColumnWidth(0, 2000)
    sheet.setColumnWidth(1, 7000)
    sheet.setColumnWidth(2, 3500)
    sheet.setColumnWidth(3, 3500)
    sheet.setColumnWidth(4, 3500)

    // --- Save ---
    val filename = "GradeReport_${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))}.xlsx"
    FileOutputStream(filename).use { wb.write(it) }
    wb.close()

    println("\nExcel file exported: $filename")
    println("Location: ${System.getProperty("user.dir")}\\$filename\n")
}

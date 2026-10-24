package com.tayseer.app.utils

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.tayseer.app.data.entity.AttendanceRecord
import com.tayseer.app.data.entity.AttendanceStatus
import com.tayseer.app.data.entity.EvaluationRecord
import com.tayseer.app.data.entity.Student
import java.io.File
import java.io.FileOutputStream

object PdfReportGenerator {

    fun generateAndShareStudentReport(
        context: Context,
        student: Student,
        sectionName: String,
        levelName: String,
        attendanceRecords: List<AttendanceRecord>,
        evaluations: List<EvaluationRecord>
    ): File? {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val bgPaint = Paint().apply { color = Color.WHITE }
        canvas.drawRect(0f, 0f, 595f, 842f, bgPaint)

        val headerPaint = Paint().apply { color = Color.parseColor("#0E7490") }
        canvas.drawRect(0f, 0f, 595f, 90f, headerPaint)

        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 22f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        val subTitlePaint = Paint().apply {
            color = Color.parseColor("#E0F2FE")
            textSize = 13f
            textAlign = Paint.Align.CENTER
        }

        canvas.drawText("تطبيق تيسير • تقرير متابعة الطالب", 297f, 42f, titlePaint)
        canvas.drawText("التقرير الشامل للحضور والغياب والتقييمات السلوكية والدراسية", 297f, 68f, subTitlePaint)

        val cardPaint = Paint().apply {
            color = Color.parseColor("#F1F5F9")
            style = Paint.Style.FILL
        }
        val cardStroke = Paint().apply {
            color = Color.parseColor("#CBD5E1")
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        canvas.drawRoundRect(30f, 110f, 565f, 210f, 12f, 12f, cardPaint)
        canvas.drawRoundRect(30f, 110f, 565f, 210f, 12f, 12f, cardStroke)

        val infoLabelPaint = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 14f
            isFakeBoldText = true
            textAlign = Paint.Align.RIGHT
        }
        val infoValuePaint = Paint().apply {
            color = Color.parseColor("#334155")
            textSize = 13f
            textAlign = Paint.Align.RIGHT
        }

        canvas.drawText("اسم الطالب: ${student.fullName}", 545f, 140f, infoLabelPaint)
        canvas.drawText("رقم الطالب: ${student.studentNumber}", 545f, 168f, infoValuePaint)
        canvas.drawText("القسم: $sectionName  |  المستوى: $levelName", 545f, 194f, infoValuePaint)

        val presentCount = attendanceRecords.count { it.status == AttendanceStatus.PRESENT }
        val absentCount = attendanceRecords.count { it.status == AttendanceStatus.ABSENT }
        val lateCount = attendanceRecords.count { it.status == AttendanceStatus.LATE }
        val permittedCount = attendanceRecords.count { it.status == AttendanceStatus.PERMITTED }
        val excusedCount = attendanceRecords.count { it.status == AttendanceStatus.EXCUSED_ABSENCE }
        val truantCount = attendanceRecords.count { it.status == AttendanceStatus.TRUANT }
        val totalDays = attendanceRecords.size
        val attendanceRate = if (totalDays > 0) ((presentCount + permittedCount) * 100 / totalDays) else 100

        val sectionTitlePaint = Paint().apply {
            color = Color.parseColor("#0E7490")
            textSize = 16f
            isFakeBoldText = true
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("ملخص الحضور والغياب (إجمالي الأيام المسجلة: $totalDays)", 565f, 245f, sectionTitlePaint)

        val startY = 265f
        val statBoxPaint = Paint().apply { style = Paint.Style.FILL }
        val statTextPaint = Paint().apply {
            textSize = 12f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }

        val stats = listOf(
            Triple("حاضر", presentCount.toString(), "#DCFCE7"),
            Triple("غائب", absentCount.toString(), "#FEE2E2"),
            Triple("متأخر", lateCount.toString(), "#FEF9C3"),
            Triple("مستأذن", permittedCount.toString(), "#E0F2FE"),
            Triple("بعذر", excusedCount.toString(), "#FFEDD5"),
            Triple("هارب", truantCount.toString(), "#F3E8FF")
        )

        var xPos = 565f - 85f
        stats.forEach { (label, count, colorHex) ->
            statBoxPaint.color = Color.parseColor(colorHex)
            canvas.drawRoundRect(xPos, startY, xPos + 80f, startY + 50f, 8f, 8f, statBoxPaint)

            statTextPaint.color = Color.parseColor("#1E293B")
            canvas.drawText(label, xPos + 40f, startY + 22f, statTextPaint)

            statTextPaint.color = Color.parseColor("#0F172A")
            statTextPaint.textSize = 15f
            canvas.drawText(count, xPos + 40f, startY + 42f, statTextPaint)
            statTextPaint.textSize = 12f

            xPos -= 88f
        }

        val ratePaint = Paint().apply {
            color = Color.parseColor("#059669")
            textSize = 14f
            isFakeBoldText = true
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("نسبة الانضباط والحضور: $attendanceRate%", 565f, 345f, ratePaint)

        canvas.drawText("ملخص التقييمات والسلوكيات المسجلة", 565f, 385f, sectionTitlePaint)

        var evalY = 415f
        if (evaluations.isEmpty()) {
            val emptyPaint = Paint().apply {
                color = Color.GRAY
                textSize = 13f
                textAlign = Paint.Align.RIGHT
            }
            canvas.drawText("لا توجد تقييمات مسجلة للطالب حتى الآن.", 565f, evalY, emptyPaint)
        } else {
            val evalItemPaint = Paint().apply {
                color = Color.parseColor("#334155")
                textSize = 12f
                textAlign = Paint.Align.RIGHT
            }
            evaluations.take(8).forEach { eval ->
                val tagLabel = if (eval.customTagTitle.isNotBlank()) eval.customTagTitle else eval.tag.defaultArabicLabel
                val line = "• [${eval.date}] $tagLabel - المعلم: ${eval.teacherName} ${if (eval.notes.isNotBlank()) "- ملاحظة: ${eval.notes}" else ""}"
                canvas.drawText(line, 565f, evalY, evalItemPaint)
                evalY += 24f
            }
        }

        val footerLine = Paint().apply {
            color = Color.parseColor("#E2E8F0")
            strokeWidth = 1.5f
        }
        canvas.drawLine(30f, 750f, 565f, 750f, footerLine)

        val footerPaint = Paint().apply {
            color = Color.parseColor("#64748B")
            textSize = 12f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("ختم الإدارة واعتماد المعلم", 150f, 780f, footerPaint)
        canvas.drawText("توقيع ولي الأمر بالعلم", 450f, 780f, footerPaint)
        canvas.drawText("تيسير - نظام إدارة الطلاب المحلي الآمن • تم إنشاء التقرير بتاريخ: ${HijriCalendarHelper.getTodayIsoDate()}", 297f, 825f, footerPaint)

        document.finishPage(page)

        val reportsDir = File(context.cacheDir, "reports")
        if (!reportsDir.exists()) reportsDir.mkdirs()
        val file = File(reportsDir, "Tayseer_Report_${student.studentNumber}.pdf")

        return try {
            val outputStream = FileOutputStream(file)
            document.writeTo(outputStream)
            outputStream.close()
            document.close()
            file
        } catch (e: Exception) {
            document.close()
            null
        }
    }

    fun sharePdfFile(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "تقرير متابعة الطالب - تطبيق تيسير")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "مشاركة تقرير الطالب عبر:"))
    }
}

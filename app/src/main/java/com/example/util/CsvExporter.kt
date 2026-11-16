package com.example.util

import com.example.model.TransactionModel
import com.example.model.TransactionType

object CsvExporter {
    fun buildTransactionsCsv(transactions: List<TransactionModel>): String {
        val sb = StringBuilder()
        // UTF-8 BOM for Excel compatibility
        sb.append('\uFEFF')
        sb.appendLine("ID,Tanggal,Tipe,Kategori,Nominal (IDR),Catatan")
        transactions.forEach { tx ->
            val typeLabel = TransactionType.fromKey(tx.type).labelId
            val safeCategory = "\"${tx.categoryName.replace("\"", "\"\"")}\""
            val safeNote = "\"${tx.note.replace("\"", "\"\"")}\""
            sb.appendLine("${tx.id},${tx.date},$typeLabel,$safeCategory,${tx.amount},$safeNote")
        }
        return sb.toString()
    }
}

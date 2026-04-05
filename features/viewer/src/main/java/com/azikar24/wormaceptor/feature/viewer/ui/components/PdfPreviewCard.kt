package com.azikar24.wormaceptor.feature.viewer.ui.components

import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.graphics.createBitmap
import com.azikar24.wormaceptor.core.ui.components.CardStyle
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorCard
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.domain.entities.PdfMetadata
import com.azikar24.wormaceptor.feature.viewer.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@Composable
fun PdfPreviewCard(
    pdfData: ByteArray,
    contentType: String?,
    onFullscreen: () -> Unit,
    onDownload: () -> Unit,
    modifier: Modifier = Modifier,
    onShowMessage: (String) -> Unit = {},
) {
    val context = LocalContext.current
    var loadState by remember { mutableStateOf<PdfLoadState>(PdfLoadState.Loading) }
    var tempFile by remember { mutableStateOf<File?>(null) }

    val pdfNoPagesMessage = stringResource(R.string.viewer_pdf_no_pages)
    val pdfLoadFailedMessage = stringResource(R.string.viewer_pdf_load_failed)

    LaunchedEffect(pdfData) {
        loadState = PdfLoadState.Loading
        withContext(Dispatchers.IO) {
            try {
                val file = File(context.cacheDir, "preview_${System.currentTimeMillis()}.pdf")
                FileOutputStream(file).use { it.write(pdfData) }
                tempFile = file

                val fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                try {
                    val renderer = PdfRenderer(fd)
                    try {
                        val pageCount = renderer.pageCount
                        if (pageCount == 0) {
                            loadState = PdfLoadState.Error(pdfNoPagesMessage)
                            return@withContext
                        }

                        val page = renderer.openPage(0)
                        val scale = PdfPreviewDefaults.ThumbnailRenderScale
                        val bitmap = createBitmap(
                            (page.width * scale).toInt(),
                            (page.height * scale).toInt(),
                        )
                        bitmap.eraseColor(android.graphics.Color.WHITE)
                        page.render(
                            bitmap,
                            null,
                            null,
                            PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY,
                        )
                        page.close()

                        val metadata = PdfMetadata(
                            pageCount = pageCount,
                            title = extractPdfTitle(pdfData),
                            fileSize = pdfData.size.toLong(),
                            version = extractPdfVersion(pdfData).orEmpty(),
                        )

                        loadState = PdfLoadState.Success(bitmap, metadata)
                    } finally {
                        renderer.close()
                    }
                } finally {
                    fd.close()
                }
            } catch (e: SecurityException) {
                loadState = PdfLoadState.PasswordProtected(
                    PdfMetadata(
                        pageCount = 0,
                        fileSize = pdfData.size.toLong(),
                        version = extractPdfVersion(pdfData).orEmpty(),
                        isEncrypted = true,
                    ),
                )
            } catch (e: Exception) {
                loadState = PdfLoadState.Error(e.message ?: pdfLoadFailedMessage)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            tempFile?.delete()
        }
    }

    WormaCeptorCard(
        onClick = onFullscreen,
        modifier = modifier.fillMaxWidth(),
        style = CardStyle.Outlined,
        borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = WormaCeptorTokens.Alpha.SOFT),
        enabled = loadState is PdfLoadState.Success,
    ) {
        when (val state = loadState) {
            is PdfLoadState.Loading -> PdfPreviewLoadingContent()
            is PdfLoadState.Success -> PdfPreviewSuccessContent(
                thumbnail = state.thumbnail,
                metadata = state.metadata,
                pdfData = pdfData,
                tempFile = tempFile,
                onFullscreen = onFullscreen,
                onDownload = onDownload,
                onShowMessage = onShowMessage,
            )

            is PdfLoadState.Error -> PdfPreviewErrorContent(message = state.message)
            is PdfLoadState.PasswordProtected -> PdfPreviewPasswordContent(
                metadata = state.metadata,
                pdfData = pdfData,
                tempFile = tempFile,
                onDownload = onDownload,
                onShowMessage = onShowMessage,
            )
        }
    }
}

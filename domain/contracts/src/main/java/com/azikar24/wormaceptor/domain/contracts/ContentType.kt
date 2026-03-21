package com.azikar24.wormaceptor.domain.contracts

/**
 * Supported content types for body parsing and display.
 */
enum class ContentType {
    /** application/json structured data. */
    JSON,

    /** application/xml or text/xml structured data. */
    XML,

    /** text/html web page content. */
    HTML,

    /** application/protobuf binary serialization. */
    PROTOBUF,

    /** application/x-www-form-urlencoded key-value pairs. */
    FORM_DATA,

    /** Multipart content with boundary-separated parts. */
    MULTIPART,

    /** text/plain unstructured text. */
    PLAIN_TEXT,

    /** Opaque binary data with no specific format. */
    BINARY,

    /** application/pdf document. */
    PDF,

    /** image/png raster image. */
    IMAGE_PNG,

    /** image/jpeg raster image. */
    IMAGE_JPEG,

    /** image/gif animated or static image. */
    IMAGE_GIF,

    /** image/webp raster image. */
    IMAGE_WEBP,

    /** image/svg+xml vector image. */
    IMAGE_SVG,

    /** image/bmp bitmap image. */
    IMAGE_BMP,

    /** image/x-icon favicon. */
    IMAGE_ICO,

    /** Image format not covered by a specific variant. */
    IMAGE_OTHER,

    /** Content type could not be determined. */
    UNKNOWN,
}

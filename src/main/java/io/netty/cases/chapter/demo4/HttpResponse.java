/**
 *
 */
package io.netty.cases.chapter.demo4;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;

public class HttpResponse {
    private final HttpHeaders header;
    private final FullHttpResponse response;
    private byte[] body;

    public HttpResponse(FullHttpResponse response) {
        this.header = response.headers();
        this.response = response;
        if (response.content() != null) {
            body = new byte[response.content().readableBytes()];
            response.content().getBytes(0, body);
        }
    }

//	public HttpResponse(FullHttpResponse response)
//	{
//		this.header = response.headers();
//		this.response = response;
//	}

    public HttpHeaders header() {
        return header;
    }

//	public byte [] body()
//	{
//		return body = response.content() != null ?
//				response.content().array() : null;
//	}

//	public byte [] body()
//	{
//		body = new byte[response.content().readableBytes()];
//		response.content().getBytes(0, body);
//		return body;
//	}

    public byte[] body() {
        return body;
    }
}

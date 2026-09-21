package dev.pinaki.backstage.library.impl.kv;

import java.io.IOException;

import dev.pinaki.backstage.library.KeyValueController;
import dev.pinaki.backstage.library.impl.http.HttpRequest;
import dev.pinaki.backstage.library.impl.http.util.ResponseUtil;

public final class KeyValuePageHandler implements KeyValueRequestHandler {
    @Override
    public boolean handle(HttpRequest request, KeyValueController controller) throws IOException {
        if (!HttpRequest.GET.equals(request.getMethod()) && !HttpRequest.HEAD.equals(request.getMethod())) {
            return ResponseUtil.methodNotAllowed(request, "GET, HEAD");
        }
        String base = js(controller.getPath());
        String body = "<!doctype html><html><head><meta charset=\"utf-8\"><meta name=\"viewport\" "
                + "content=\"width=device-width,initial-scale=1\"><title>" + html(controller.getTitle())
                + "</title><style>" + styles() + "</style></head><body><main><nav><a href=\"/backstage\">"
                + "← Dashboard</a></nav><h1>" + html(controller.getTitle()) + "</h1>"
                + "<form id=\"create\"><input name=\"key\" placeholder=\"Key\" required>"
                + "<input name=\"value\" placeholder=\"Value\"><button>Add</button></form>"
                + "<table><thead><tr><th>Key</th><th>Value</th><th></th></tr></thead>"
                + "<tbody id=\"rows\"></tbody></table><button id=\"clear\" class=\"danger\">Clear all</button>"
                + "</main><script>const base='" + base + "',rows=document.querySelector('#rows');"
                + "const enc=o=>new URLSearchParams(o).toString();function render(data){rows.replaceChildren("
                + "...Object.entries(data).sort().map(([k,v])=>{const tr=document.createElement('tr'),"
                + "key=document.createElement('td'),val=document.createElement('td'),actions=document.createElement('td');"
                + "key.textContent=k;const input=document.createElement('input');input.value=v;val.append(input);"
                + "const save=document.createElement('button');save.textContent='Save';save.onclick=()=>fetch(base+'/api',"
                + "{method:'PUT',headers:{'Content-Type':'application/x-www-form-urlencoded'},body:enc({key:k,value:input.value})});"
                + "const del=document.createElement('button');del.textContent='Delete';del.className='danger';"
                + "del.onclick=()=>fetch(base+'/api?key='+encodeURIComponent(k),{method:'DELETE'});actions.append(save,del);"
                + "tr.append(key,val,actions);return tr;}));}new EventSource(base+'/events').onmessage=e=>render(JSON.parse(e.data));"
                + "document.querySelector('#create').onsubmit=e=>{e.preventDefault();fetch(base+'/api',{method:'POST',"
                + "headers:{'Content-Type':'application/x-www-form-urlencoded'},body:new URLSearchParams(new FormData(e.target))});"
                + "e.target.reset();};document.querySelector('#clear').onclick=()=>confirm('Delete all entries?')&&"
                + "fetch(base+'/api',{method:'DELETE'});</script></body></html>";
        return ResponseUtil.html(request, body);
    }

    private static String html(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;");
    }

    private static String js(String value) {
        return value.replace("\\", "\\\\").replace("'", "\\'").replace("<", "\\u003c");
    }

    private static String styles() {
        return "body{font:16px system-ui;background:#f5f5f7;color:#202124;margin:0}main{max-width:900px;"
                + "margin:32px auto;padding:24px}form{display:flex;gap:8px;margin:24px 0}input{padding:9px;"
                + "border:1px solid #bbb;border-radius:6px}table{width:100%;border-collapse:collapse;background:white;"
                + "margin-bottom:18px}th,td{text-align:left;padding:10px;border-bottom:1px solid #ddd}button{padding:8px 12px;"
                + "margin:2px;border:0;border-radius:6px;background:#1769aa;color:white}.danger{background:#b3261e}"
                + "nav a{color:#1557b0}";
    }
}

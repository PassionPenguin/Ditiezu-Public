/* Made by Penguin */
window.cE = (data) => {
    let e = document.createElement(data.type);
    if (typeof data.attr !== "undefined")
        for (let i = 0; i < data.attr.length; i++)
            e.setAttribute(data.attr[i][0], data.attr[i][1]);
    if (typeof data.innerText !== "undefined")
        e.innerText = typeof (data.innerText) === "object" ? data.innerText[cL] : data.innerText;
    if (typeof data.innerHTML !== "undefined")
        e.innerHTML = typeof (data.innerHTML) === "object" ? data.innerHTML[cL] : data.innerHTML;
    if (typeof data.onclick !== "undefined")
        e.onclick = () => {
            data.onclick()
        };
    return e;
};
window.Int = parseInt;
Node.prototype.append = Node.prototype.appendChild;
window.pgFocus = (a) => {
    return a.getAttribute("pg-active");
};
if (!Array.prototype.last) {
    Array.prototype.last = function () {
        return this[this.length - 1];
    };
}
window.importScripts = (url, callback) => {
    let script = cE({type: "script", attr: [["src", url]]});
    document.body.append(script);
    callback !== undefined ? script.onload = script.onreadystatechange = () => {
        callback();
    } : void (0);
};
window.getPara = (para, url) => {
    url = url || window.location.href;
    let result = null,
        tmp = [];
    url.substr(url.indexOf("?"))
        .substr(1)
        .split("&")
        .forEach(function (item) {
            tmp = item.split("=");
            if (tmp[0] === para) result = decodeURIComponent(tmp[1]);
        });
    return result;
};
window.pg = {
    $: (selector) => {
        return document.querySelectorAll(selector);
    },
    alert: (message, log, occur, returnFunc, opt) => {
        if (opt === undefined)
            opt = {smalltext: false};
        let alertWrap = cE({type: "div", attr: [["class", "pg-alert"]]});
        document.body.append(alertWrap);
        alertWrap.append(cE({type: "p", attr: [["class", "pg-alert-content"]], innerText: message}));
        alertWrap.append(cE({type: "p", attr: [["class", "pg-alert-log"]], innerText: log}));
        alertWrap.append(cE({
            type: "p",
            attr: [["class", "pg-alert-occ"]],
            innerText: "Occured at " + occur + ", Log will be hidden in 2.5s."
        }));
        if (opt.smalltext === true)
            alertWrap.children[0].classList.add("small");
        setTimeout(() => {
            alertWrap.style.opacity = "0";
            setTimeout((() => {
                document.body.removeChild(alertWrap);
                if (returnFunc !== undefined)
                    returnFunc();
            }), 500);
        }, 2500)
    },
    confirm: (message, log, returnFunc) => {
        let confirmWrap = cE({type: "div", attr: [["class", "pg-confirm"]]});
        document.body.append(confirmWrap);
        confirmWrap.append(cE({type: "p", attr: [["class", "pg-confirm-content"]], innerText: message}));
        confirmWrap.append(cE({type: "p", attr: [["class", "pg-confirm-log"]], innerText: log}));
        confirmWrap.append(cE({
            type: "div",
            attr: [["class", "pg-confirm-selector"]],
            innerHTML: "<div class='cancel'><span class='mi'>cancel</span>取消</div><div class='confirm'><span class='mi'>check</span>确认</div>"
        }));
        confirmWrap.lastElementChild.onclick = () => {
            returnFunc(true);
            confirmWrap.style.opacity = "0";
            setTimeout((() => {
                document.body.removeChild(confirmWrap);
            }), 500);
        };
        confirmWrap.firstElementChild.onclick = () => {
            returnFunc(false);
            confirmWrap.style.opacity = "0";
            setTimeout((() => {
                document.body.removeChild(confirmWrap);
            }), 500);
        };
    },
    notification: (type, msg, status, returnFunc) => {
        let notification = cE({type: "div", attr: [["class", "pg-notification"]]});
        document.body.append(notification);
        notification.append(cE({type: "p", attr: [["class", "pg-notification-type"]], innerText: type}));
        notification.append(cE({type: "p", attr: [["class", "pg-notification-log"]], innerText: msg}));
        notification.append(cE({
            type: "p",
            attr: [["class", "pg-notification-occ"]],
            innerText: status
        }));
        if (returnFunc === undefined)
            setTimeout(() => {
                notification.style.opacity = "0";
                setTimeout((() => {
                    document.body.removeChild(notification);
                }), 500);
            }, 2500)
        else
            notification.onclick = () => {
                notification.style.opacity = "0";
                setTimeout((() => {
                    document.body.removeChild(notification);
                }), 500);
                returnFunc();
            }
    },
};

window.XMLParser = (feeds, request) => {
    let items = [...request.responseXML.children[0].children[0].children].filter(i => i.tagName === "item").map(i => i.children);
    items.forEach(e => {
        let feed = cE({type: "div", attr: [["class", "pg-feed"]]});
        e = [...e];
        feed.onclick = () => {
            let url = e.filter(i => i.tagName === "link")[0].innerHTML.replace(/&amp;/, "&");
            console.log(url);
            loadThread(url.substring(url.indexOf("&tid=") + 5), 1);
        };
        feed.append(cE({
            type: "p",
            innerText: e.filter(i => i.tagName === "title")[0].innerHTML,
            attr: [["class", "pg-feed-title"]]
        }));
        let feedContent = cE({type: "div", attr: [["class", "pg-feedContent"]]});
        let description = e.filter(i => i.tagName === "description")[0].innerHTML;
        feedContent.append(cE({
            type: "p",
            innerText: description.substr(9
                , description.length - 12).replace(/\n/ig, " ").replace(/ \s\s\s/ig, "\n").replace(/\n\n/, "\n"), /* Ignore too many breaks */
            attr: [["class", "pg-feed-description"]]
        }));
        feed.append(feedContent);
        let enclosure = e.filter(i => i.tagName === "enclosure");
        if (enclosure.length !== 0 && description.substr(9
            , description.length - 12).replace(/\n/ig, " ").replace(/ \s\s\s/ig, "\n").replace(/\n\n/, "\n") !== "")
            feed.append(cE({
                type: "div",
                attr: [["class", "pg-feed-enclosure"], ["style", "background-image:url(\"" + enclosure[0].getAttribute("url") + "\")"]]
            }));
        else if (description.substr(9
            , description.length - 12).replace(/\n/ig, " ").replace(/ \s\s\s/ig, "\n").replace(/\n\n/, "\n") === "")
            feed.removeChild(feedContent);
        else
            feedContent.classList.add("no-image");
        feed.append(cE({
            type: "p",
            innerText: e.filter(i => i.tagName === "author")[0].innerHTML + " - " + e.filter(i => i.tagName === "category")[0].innerHTML + " - " + new Date(e.filter(i => i.tagName === "pubDate")[0].innerHTML).toLocaleString("zh-CN", {timeZone: "Asia/Hong_Kong"}),
            attr: [["class", "pg-feed-metaInfo"]]
        }));
        feeds.append(feed);
    });
};
window.loadUrl = (url) => {
    // window.location.href = url;
    console.log("Caught Redirect:\t" + url);
};
window.zoomImage = (e, src) => {
    let removeZoomImage = () => {
        document.body.removeChild(imageUtilWrap);
        document.body.removeChild(zoomImageUtilCtrlWrap);
        document.body.removeChild(bottomZoomImageUtilCtrlWrap);
        document.body.removeChild(pg.$("#pg-filter-zoomFilter")[0]);
        document.body.removeChild(pg.$("#zoomImage")[0]);
        pg.$("#pg-app")[0].style.filter = "none";
        pg.$("#pg-app")[0].style.position = "unset";
    }
    document.body.append(cE({
        type: "div",
        attr: [["style", "width:200%;height:200%;position:fixed;left:0;top:0;z-index:1000;background:var(--black500);left:0;top:0;"], ["id", "pg-filter-zoomFilter"]],
        onclick: () => {
            removeZoomImage()
        }
    }));
    pg.$("#pg-app")[0].style.filter = "blur(10px)";
    pg.$("#pg-app")[0].style.position = "fixed";
    document.body.append(cE({
        type: "img",
        attr: [["src", src], ["style", "position:fixed;left:50%;top:50%;transform:translate(-50%,-50%);width:100%;max-width:100%;max-height:100%;z-index:1000;"], ["id", "zoomImage"]]
    }));
    let imageUtilWrap = cE({
        type: "div",
        attr: [["style", "position:fixed;top:0;left:0;width:100%;height:48px;z-index:1002;background:var(--white);"], ["id", "zoomImageUtilWrap"]]
    });
    imageUtilWrap.append(cE({
        type: "div",
        attr: [["class", "mi"], ["style", "display: inline-block;position: absolute;left: 20px;top: 50%;transform: translate(0, -50%);"]],
        onclick: () => {
            removeZoomImage()
        }, innerText: "chevron_left"
    }));
    imageUtilWrap.append(cE({
        type: "div",
        attr: [["class", "mi"], ["style", "display: inline-block;position: absolute;right: 20px;top: 50%;transform: translate(0, -50%);"]],
        onclick: () => {
            zoomImageUtilCtrlWrap.classList.add("active");
        }, innerText: "more_vert"
    }));
    imageUtilWrap.append(cE({
        type: "div",
        attr: [["style", "display: inline-block;position: absolute;left: 50%;top: 50%;transform: translate(-50%, -50%);z-index:1002;font: 15px/1 Anodina,sans-serif;"]],
        onclick: () => {
            removeZoomImage()
        },
        innerText: src.substring(src.lastIndexOf("/") + 1).length > 25 ? src.substring(src.lastIndexOf("/") + 1).substring(0, 10) + "..." + src.substring(src.lastIndexOf("/") + 1).substring(src.substring(src.lastIndexOf("/") + 1).length - 10) : src.substring(src.lastIndexOf("/") + 1)
    }));
    document.body.append(imageUtilWrap);
    let zoomImageUtilCtrlWrap = cE({type: "div", attr: [["id", "zoomImageUtilCtrlWrap"]]});
    document.body.append(zoomImageUtilCtrlWrap);
    zoomImageUtilCtrlWrap.append(cE({
        type: "div", attr: [["class", "mi"]], onclick: () => {
            sys.shareImage(src);
        }, innerHTML: "share"
    }));
    zoomImageUtilCtrlWrap.append(cE({
        type: "div", attr: [["class", "mi"]], onclick: () => {
            sys.downloadFile(src);
        }, innerHTML: "save"
    }));
    zoomImageUtilCtrlWrap.append(cE({
        type: "div", attr: [["class", "mi"]], onclick: () => {
            pg.alert("文件真实路径：" + src + "\n文件名称：" + src.substring(src.lastIndexOf("/") + 1), "", "Showing Detailed Info", () => {
            }, {smalltext: true})
        }, innerHTML: "info"
    }));
    let bottomZoomImageUtilCtrlWrap = cE({
        type: "div",
        attr: [["style", "position:fixed;left:0;bottom:0;width:100%;height:48px;font-size:0;background:var(--white);z-index:1001;"]]
    })
    bottomZoomImageUtilCtrlWrap.append(cE({
        type: "div",
        attr: [["style", "display:inline-block;width:25%;text-align:center;vertical-align:middle;"]],
        innerHTML: "<span class='mi' style='line-height: 48px'>rotate_left</span>",
        onclick: () => {
            if (pg.$("#zoomImage")[0].style.transform.includes("rotate")) {
                let degree = Int(pg.$("#zoomImage")[0].style.transform.match(/\d+deg/)[0].replace(/deg/, ""));
                pg.$("#zoomImage")[0].style.transform = pg.$("#zoomImage")[0].style.transform.replace(/rotate\(\d+deg\)/, "rotate(") + (degree - 90 < 0 ? degree + 270 : degree - 90).toString() + "deg)";
            } else
                pg.$("#zoomImage")[0].style.transform += "rotate(270deg)";
        }
    }));
    let scale_big = () => {
        if (pg.$("#zoomImage")[0].style.transform.includes("scale")) {
            let degree = parseFloat(pg.$("#zoomImage")[0].style.transform.match(/scale\([-+]?[0-9]*\.?[0-9]*/)[0].replace(/scale\(/, ""));
            if (degree + 0.25 >= 1.75) {
                scaleBig.onclick = () => {
                };
                scaleBig.children[0].style.color = "var(--grey)";
            }
            if (degree >= 0.25) {
                scaleSmall.children[0].style.color = "var(--black)";
                scaleSmall.onclick = () => {
                    scale_small()
                }
            }
            pg.$("#zoomImage")[0].style.transform = pg.$("#zoomImage")[0].style.transform.replace(/scale\([-+]?[0-9]*\.?[0-9]*\)/, "scale(") + (degree + 0.25).toString() + ")";
        } else
            pg.$("#zoomImage")[0].style.transform += "scale(1.25)";
    }
    let scaleBig = cE({
        type: "div",
        attr: [["style", "display:inline-block;width:25%;text-align:center;vertical-align:middle;"]],
        innerHTML: "<span class='mi' style='line-height: 48px'>add</span>",
        onclick: () => {
            scale_big();
        }
    });
    bottomZoomImageUtilCtrlWrap.append(scaleBig);
    let scale_small = () => {
        if (pg.$("#zoomImage")[0].style.transform.includes("scale")) {
            let degree = parseFloat(pg.$("#zoomImage")[0].style.transform.match(/scale\([-+]?[0-9]*\.?[0-9]*/)[0].replace(/scale\(/, ""));
            if (degree - 0.25 <= 0.25) {
                scaleSmall.onclick = () => {
                };
                scaleSmall.children[0].style.color = "var(--grey)";
            }
            if (degree <= 1.75) {
                scaleBig.children[0].style.color = "var(--black)";
                scaleBig.onclick = () => {
                    scale_big()
                }
            }
            pg.$("#zoomImage")[0].style.transform = pg.$("#zoomImage")[0].style.transform.replace(/scale\([-+]?[0-9]*\.?[0-9]*\)/, "scale(") + (degree - 0.25).toString() + ")";
        } else
            pg.$("#zoomImage")[0].style.transform += "scale(0.75)";
    }
    let scaleSmall = cE({
        type: "div",
        attr: [["style", "display:inline-block;width:25%;text-align:center;vertical-align:middle;"]],
        innerHTML: "<span class='mi' style='line-height: 48px'>remove</span>",
        onclick: () => {
            scale_small();
        }
    });
    bottomZoomImageUtilCtrlWrap.append(scaleSmall);
    bottomZoomImageUtilCtrlWrap.append(cE({
        type: "div",
        attr: [["style", "display:inline-block;width:25%;text-align:center;vertical-align:middle;"]],
        innerHTML: "<span class='mi' style='line-height: 48px'>rotate_right</span>",
        onclick: () => {
            if (pg.$("#zoomImage")[0].style.transform.includes("rotate")) {
                let degree = Int(pg.$("#zoomImage")[0].style.transform.match(/\d+deg/)[0].replace(/deg/, ""));
                pg.$("#zoomImage")[0].style.transform = pg.$("#zoomImage")[0].style.transform.replace(/rotate\(\d+deg\)/, "rotate(") + (degree - 90 > 360 ? degree - 270 : degree + 90).toString() + "deg)";
            } else
                pg.$("#zoomImage")[0].style.transform += "rotate(90deg)";
        }
    }));
    document.body.append(bottomZoomImageUtilCtrlWrap)
}
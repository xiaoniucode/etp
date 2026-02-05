var ClipMaster = (function () {
    let hiddenInput = null;
    let lastSuccessCallback = null;

    function getHiddenInput() {
        if (!hiddenInput || !document.body.contains(hiddenInput)) {
            hiddenInput = document.createElement('textarea');
            hiddenInput.style.cssText = 'position:fixed;top:-99px;left:-99px;opacity:0;pointer-events:none;z-index:-1;';
            hiddenInput.setAttribute('readonly', '');
            hiddenInput.setAttribute('tabindex', '-1');
            document.body.appendChild(hiddenInput);
        }
        return hiddenInput;
    }

    function execCopy(text) {
        const input = getHiddenInput();
        input.value = text;
        input.select();
        input.setSelectionRange(0, 999999);

        let success = false;
        try {
            success = document.execCommand('copy');
        } catch (e) {
            console.warn( e);
        }

        if (document.activeElement && document.activeElement.blur) {
            document.activeElement.blur();
        }

        return success;
    }

    function tryModernCopy(text, onSuccess) {
        if (
            navigator.clipboard &&
            typeof navigator.clipboard.writeText === 'function' &&
            (location.protocol === 'https:' || /^(127\.0\.0\.1|localhost)$/.test(location.hostname))
        ) {
            navigator.clipboard.writeText(text).then(
                function () {
                    fireSuccess(onSuccess, text);
                },
                function () {
                    fallbackCopy(text, onSuccess);
                }
            );
        } else {
            fallbackCopy(text, onSuccess);
        }
    }

    function fallbackCopy(text, onSuccess) {
        if (execCopy(text)) {
            fireSuccess(onSuccess, text);
        } else {
            showManualCopyLayer(text, onSuccess);
        }
    }

    function fireSuccess(onSuccess, text) {
        if (typeof onSuccess === 'function') {
            lastSuccessCallback = onSuccess;
            setTimeout(() => {
                onSuccess(text);
                lastSuccessCallback = null;
            }, 50);
        }
    }

    function showManualCopyLayer(text, onSuccess) {
        layer.open({
            title: '请手动复制',
            type: 1,
            area: ['420px', '180px'],
            shadeClose: true,
            resize: false,
            content: '<div style="padding:20px;"><input type="text" value="' + text.escapeHtml() + '" id="clipmaster_manual_input" style="width:100%;padding:8px;font-size:14px;" onclick="this.select()"></div>',
            success: function () {
                setTimeout(() => {
                    const input = document.getElementById('clipmaster_manual_input');
                    if (input) {
                        input.select();
                        input.focus();
                        if (execCopy(text)) {
                            layer.closeAll();
                            fireSuccess(onSuccess, text);
                        }
                    }
                }, 100);
            }
        });

        if (window.__CLIPMASTER_LOG__) {
            window.__CLIPMASTER_LOG__('copy_fallback_manual', {length: text.length});
        }
    }

    String.prototype.escapeHtml = function () {
        const div = document.createElement('div');
        div.textContent = this;
        return div.innerHTML;
    };

    return {
        /**
         * 复制文本
         * @param {string} text 要复制的内容
         * @param {function} onSuccess 只有成功时才会调用，参数为复制的文本
         */
        copy: function (text, onSuccess) {
            if (!text) {
                layer.msg('复制内容为空', {icon: 2});
                return;
            }
            if (typeof onSuccess !== 'function') {
                onSuccess = function () {
                    layer.msg('复制成功', {icon: 1, time: 1200});
                };
            }
            tryModernCopy(String(text), onSuccess);
        },
    };
})();

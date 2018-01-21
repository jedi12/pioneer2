var ajaxResponse;
function openLoadingDialog() {
    ajaxResponse = false;
    setTimeout(function() {
        if (ajaxResponse == false) {
            PF('loading').show();
        }
    }, 800);
}

function closeLoadingDialog() {
    ajaxResponse = true;
    PF('loading').hide();
}

function initClientProp() {
    document.getElementById("initProp:screenHeight").value = screen.height;
    document.getElementById("initProp:screenWidth").value = screen.width;
    document.getElementById("initProp:zoneOffset").value = 0 - new Date().getTimezoneOffset();
}

function formatMenuTabs() {
    PF('menuAccordion').panels.each(function() {
        if (this.children.length == 0) {
            this.style.padding = "0";
            this.style.margin = "0";
        }
    })
}

function shiftDialog() {
    var dialog = getTopVisibleDialog();
    dialog.css('left', dialog.offset().left + 40 + "px");
}

function formatDialog() {
    var dialog = getTopVisibleDialog();
    var tabs = dialog.find('.ui-tabs-panel');
    if (tabs.length == 0) {
        formatPanel(dialog);
    } else {
        formatTabs(dialog);
    }
    formatChoiceList();

    dialog.position({my: 'center', at: 'center', of: window})
}

function formatTabs(dialog) {
    var tabs = dialog.find('.ui-tabs-panel');
    var maxTabsHeight = Math.max.apply(null, tabs.map(function() {
        return $(this).height();
    }));
    tabs.height(maxTabsHeight);

    var winHeight = $(window).height();
    var dialogHeight = dialog.height();
    var diff = dialogHeight - winHeight;

    if (diff > 0) {
        tabs.height(maxTabsHeight - diff);
        tabs.find('.scrollPanel').css('overflow-y', 'auto');
    }
}

function formatPanel(dialog) {
    var scrollPanel = dialog.find('.scrollPanel');

    var winHeight = $(window).height();
    var dialogHeight = dialog.height();
    var diff = dialogHeight - winHeight;

    if (diff > 0) {
        scrollPanel.height(scrollPanel.height() - diff);
        scrollPanel.css('overflow-y', 'auto');

    }
}

function formatChoiceList() {
    var selectOneMenus = getTopVisibleDialog().find('.ui-selectonemenu').filter(":visible");
    selectOneMenus.each(function() {
        var parentPanelWidth = $(this).parents('table').parent().width();
        var tableWidth = $(this).parents('table').width();
        var parentWidth = $(this).parent().width();
        var width = $(this).width();
        var diff = tableWidth - parentPanelWidth;
        if (diff > 0) {
            $(this).css('max-width', (width - diff) + 'px');
            $(this).css('min-width', (width - diff) + 'px');
        } else {
            $(this).css('max-width', (parentWidth - 30) + 'px');
            $(this).css('min-width', '0px');
        }
    });
}

function getTopVisibleDialog() {
    var visibleDialogs = $('.ui-dialog').filter(":visible");
    var maxZIndex = Math.max.apply(null, visibleDialogs.map(function() {
        var z;
        return isNaN(z = parseInt($(this).css("z-index"), 10)) ? 0 : z;
    }));
    var topVisibleDialog;
    visibleDialogs.each(function() {
        if ($(this).css("z-index") == maxZIndex) {
            topVisibleDialog = $(this);
        }
    });
    return topVisibleDialog ? topVisibleDialog : visibleDialogs;
}

// Костыль, для <p:calendar> не дает открываться, если это поле в диалоге идет первым
PrimeFaces.widget.Dialog.prototype.applyFocus = function() {
    var firstInput = this.jq.find(':not(:submit):not(:button):input:visible:enabled:first');
    if(!firstInput.hasClass('hasDatepicker')) {
        firstInput.focus();
    }
};

PrimeFaces.locales ['ru_RU'] = {
    closeText: 'Закрыть',
    prevText: 'Назад',
    nextText: 'Вперёд',
    monthNames: ['Январь', 'Февраль', 'Март', 'Апрель', 'Май', 'Июнь', 'Июль', 'Август', 'Сентябрь', 'Октябрь', 'Ноябрь', 'Декабрь'],
    monthNamesShort: ['Янв', 'Фев', 'Мар', 'Апр', 'Май', 'Июн', 'Июл', 'Авг', 'Сен', 'Окт', 'Ноя', 'Дек'],
    dayNames: ['Воскресенье', 'Понедельник', 'Вторник', 'Среда', 'Четверг', 'Пятница', 'Суббота'],
    dayNamesShort: ['Воск','Пон' , 'Вт' , 'Ср' , 'Четв' , 'Пят' , 'Суб'],
    dayNamesMin: ['Вс', 'Пн', 'Вт', 'Ср', 'Чт', 'Пт', 'Сб'],
    weekHeader: 'Неделя',
    firstDay: 1,
    isRTL: false,
    showMonthAfterYear: false,
    yearSuffix:'',
    timeOnlyTitle: 'Только время',
    timeText: 'Время',
    hourText: 'Час',
    minuteText: 'Минута',
    secondText: 'Секунда',
    currentText: 'Сегодня',
    ampm: false,
    month: 'Месяц',
    week: 'Неделя',
    day: 'День',
    allDayText: 'Весь день'
};

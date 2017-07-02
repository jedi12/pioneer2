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

function formatDialog() {
    var winHeight = $(window).height();
    var dialogHeight = $(PF('docDialog').jqId).height();
    var panelHeight = $(PF('fieldsPanel').jqId).height();

    if (dialogHeight > winHeight) {
        $(PF('fieldsPanel').jqId).css('overflow','auto');
        $(PF('fieldsPanel').jqId).height(panelHeight - dialogHeight + winHeight);
    }
}

function selectOneMenuCut() {
    selectOneMenuList = document.getElementsByClassName('ui-selectonemenu');
    for (j = 0; j < selectOneMenuList.length; ++j) {
        element = selectOneMenuList[j];
        selectOneMenuWidth = element.clientWidth;
        elementParentNode = element.parentNode;
        while (elementParentNode != 'undefined') {
            if (elementParentNode.tagName == 'TABLE') {
                tableWidth = elementParentNode.clientWidth;
                prop = window.getComputedStyle(elementParentNode.parentNode, null);
                panelWidth = prop.getPropertyValue('width').replace("px","");
                if (tableWidth > panelWidth) {
                    pribavka = tableWidth - panelWidth;
                    element.style['width'] = selectOneMenuWidth - pribavka + 'px';
                    element.style['min-width'] = '';
                }
                break;
            }
            else {
                elementParentNode = elementParentNode.parentNode;
            }
        }
    }
}

// Костыль, для <p:calendar> не дает открываться, если это поле в диалоге идет первым
PrimeFaces.widget.Dialog.prototype.applyFocus = function() {
    var firstInput = this.jq.find(':not(:submit):not(:button):input:visible:enabled:first');
    if(!firstInput.hasClass('hasDatepicker')) {
        firstInput.focus();
    }
}

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

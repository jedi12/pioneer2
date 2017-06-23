function getScreen() {

}

function onTabChange(panel) {
    if (panel[0].textContent == '') {
        panel[0].style.padding = '0';
        panel[0].style.margin = '0';
    }
}

function setScrollBottom() {
    var scrollBody = PF('collectedTable').scrollBody[0];
    scrollBody.scrollTop = scrollBody.scrollHeight;
}

PrimeFaces.locales ['ru'] = {
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

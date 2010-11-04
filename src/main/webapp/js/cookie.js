/* ------------------------------------------------------------------------------- */
/* - This file is deprecated and will be removed or replaced in future releases. - */
/* ------------------------------------------------------------------------------- */

var Cookie = {

    read: function(name) {
        var nameEQ = name + "=";
        var ca = document.cookie.split(';');
        for (var i = 0; i < ca.length; i++) {
            var c = ca[i];
            while (c.charAt(0) == ' ') c = c.substring(1, c.length);
            if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length, c.length);
        }
        return null;
    },

    write: function(name, value, expiresInMinutes) {
        var exp = new Date(new Date().getTime() + (expiresInMinutes * 60 * 1000));
        document.cookie = name + '=' + value + '; expires=' + exp.toGMTString() + ';';
    },

    remove: function(name) {
        document.cookie = name + '=; expires=Thu, 01-Jan-70 00:00:01 GMT;';
    }
};
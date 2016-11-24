(function() {
    'use strict';
    angular
        .module('dotsubApp')
        .factory('Item', Item);

    Item.$inject = ['$resource', 'DateUtils'];

    function Item ($resource, DateUtils) {
        var resourceUrl =  'api/items/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                        data.creationDate = DateUtils.convertDateTimeFromServer(data.creationDate);
                    }
                    return data;
                }
            },
            'update': { method:'PUT' }
        });
    }
})();

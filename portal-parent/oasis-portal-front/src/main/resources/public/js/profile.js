$(document).ready(function () {
    
	initBindings();

	function initBindings($el) {
		
		// Data layouts
		$('#layouts button.btn-edit', $el).click(function() {
			toggleProfileLayout($(this).attr('data'), 'EDIT');
		});
		$('#layouts button.btn-view', $el).click(function() {
			toggleProfileLayout($(this).attr('data'), 'VIEW');
		});
		$('#layouts form', $el).submit(function(e) {
			e.preventDefault();
			$form = $(this);
			saveLayout($form.attr('data'), $form.serialize());
		});
		
		// Edit avatar
    	$('#btn-edit-avatar', $el).click(function(e) {
    		$('#modal-edit-avatar').modal('show');
    		return false;
    	})
		$('.action-select-avatar', $el).click(function(e) {
			e.preventDefault();
			$('.action-select-avatar').removeClass('selected');
			$(this).addClass('selected');
    		$('#selected-avatar').val($(this).attr('src'));
    		return false;
		});
		$('#form-edit-avatar', $el).submit(function(e) {
			e.preventDefault();
			$form = $(this);
			$.ajax({
				url: $form.attr('action'),
				method: 'POST',
				data: $form.serialize(),
				success: refreshAccountData
			});
		});
		
		// Edit language
    	$('#btn-edit-language', $el).click(function(e) {
    		$('#modal-edit-language').modal('show');
    		return false;
    	})
    	$('.action-select-language', $el).click(function(e) {
    		var $this = $(this);
    		$('#selected-language').val($this.attr('data'));
    		$('#selected-language-label').html($this.html());
    	});
		$('#form-edit-language', $el).submit(function(e) {
			e.preventDefault();
			$form = $(this);
			$.ajax({
				url: $form.attr('action'),
				method: 'POST',
				data: $form.serialize(),
				success: refreshAccountData
			});
		});

		// Edit email
    	$('#btn-edit-email', $el).click(function(e) {
    		console.log($('#modal-edit-email'));
    		$('#modal-edit-email').modal('show');
    		return false;
    	})
		$('#form-edit-email', $el).submit(function(e) {
			e.preventDefault();
			$form = $(this);
			$.ajax({
				url: $form.attr('action'),
				method: 'POST',
				data: $form.serialize(),
				success: refreshAccountData
			});
		});
		
	}
	
	function toggleProfileLayout(id, mode) {
		$.ajax({
			url: '/my/profile/mode',
			method: 'POST',
			data: {
				id: id,
				mode: mode
			},
			success: function(data) {
				$('#' + id).replaceWith(data);
				initBindings($('#' + id));
			}
		})
	}
	
	function saveLayout(id, data) {
		$.ajax({
			url: '/my/profile/save',
			method: 'POST',
			data: data,
			success: function(data) {
				$('#' + id).replaceWith(data);
				initBindings($('#' + id));
			}
		})
	}
	
	function refreshAccountData(html) {
		$('#account-data').replaceWith(html);
		initBindings($('#account-data'));
		$('.modal-backdrop').remove();
	}

});
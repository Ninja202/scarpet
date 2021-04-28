__command() -> print(player(), str('Use a %s to set positions, use the draw commands to draw shapes, and distance to querry distance between positions 1 and 2. You can throw a snow ball to erase a connected object.', global_tool));

global_show_pos = true;
global_tool = 'golden_sword';

__randomise(base_material, salt_material, distribution) -> (
	dim = player() ~ 'dimension';
	pos1 = global_positions:dim:0;
	pos2 = global_positions:dim:1;

	volume(pos1:0, pos1:1, pos1:2, pos2:0, pos2:1, pos2:2,
		current = block(_);
		if(current == block(base_material) && rand(distribution),
			__set_and_save(_, block(salt_material))
		);
	);
);

random(base_material, salt_material, distribution) -> __randomise(base_material, salt_material, distribution);

////// Handle Markers //////

// Spawn a marker
__mark(i, position, dim) -> (
 	colours = l('red', 'light_blue');
	e = create_marker('pos' + i, position + l(0.5, 0.5, 0.5), colours:(i-1) + '_concrete'); // crete the marker
	run(str( //modify some stuff to make it fancier
		'data merge entity %s {Glowing:1b, Fire:32767s, Marker:1b}', query(e, 'uuid')
		));
	global_armor_stands:dim:(i-1) =  query(e, 'id'); //save the id for future use
	if(global_debug, print('Set mark') );
);

__remove_mark(i, dim) -> (
	e = entity_id(global_armor_stands:dim:(i));
 	if(e != null, modify(e, 'remove'));
);

get_armor_stands() -> print(global_armor_stands);

// set a position
set_pos(i) -> (
	dim = player() ~ 'dimension';

	try( // position index must be 1, 2 or 3
 		if( !reduce(range(1,4), _a + (_==i), 0),
			throw();
		),
		print(format('rb Error: ', 'y Input must be either 1, 2 or 3 for position to set. You input ' + i) );
		return()
	);
	// position to be set at the block the player is aiming at, or player position, if there is none
	tha_block = query(player(), 'trace');
	if(tha_block!=null,
		tha_pos = pos(tha_block),
		tha_pos = map(pos(player()), round(_))
	);
	global_positions:dim:(i-1) = tha_pos; // save to global positions
	__all_set(dim);

	print(str('Set your position %d in %s to ',i, dim) + tha_pos);

	if(global_show_pos, // remove previous marker for set positi, if aplicable
		__remove_mark(i-1, dim); //-1 because stupid indexes
		__mark(i, tha_pos, dim);
	);

);

// print list of positions
get_pos() -> (
	dim = player() ~ 'dimension';
	for(global_positions:dim,
 		print(str('Position %d is %s',
				_i+1, if(_==null, 'not set', _)));
 	)
);

// toggle markers and bounding box visibility
toggle_show_pos() ->(
	dim = player() ~ 'dimension';
	global_show_pos = !global_show_pos;
	if(global_show_pos,
		( // summon the markers
			for(global_positions:dim,
				if(_!=null, __mark( (_i+1) , _, dim) );
			);
			print('Positions are now shown');
		),
		// else
		( //remove the markers
			for(global_armor_stands:dim,
				__remove_mark(_i, dim);
			);
			print('Positions are now hidden');
		);
	);
);

// remove all markers
__reset_positions(dim) -> (
	loop(3,
		__remove_mark(_, dim);
	);
	global_positions:dim = l(null, null);
	global_all_set:dim = false;
	global_armor_stands:dim = l(null, null);
);

reset_positions() -> (
	dim = player() ~ 'dimension';
	__reset_positions(dim);
);

// set position 1 if player left clicks with a golden sword
__on_player_clicks_block(player, block, face) -> (
	if(player~'holds':0 == global_tool,
		set_pos(1);
	);
);

// set position 2 if player right clicks with a golden sword
__on_player_uses_item(player, item_tuple, hand) -> (
	if(
	item_tuple:0 == global_tool,
	set_pos(2)
	)
);

__all_set(dim) -> (
	if(all(global_positions:dim, _!=null), global_all_set:dim = true);
);

global_positions = m();
global_all_set = m();
global_armor_stands = m();

__reset_positions('overworld');
__reset_positions('the_nether');
__reset_positions('the_end');



////// Undo stuff ///////

global_history = {
					'overworld' -> [] ,
					'the_nether' -> [] ,
					'the_end' -> [] ,
				};

__set_and_save(block, material) -> ( //defaults to no replace
	global_this_story:length(global_this_story) = [pos(block), block];
	set(block , material);
);

__put_into_history(story, dim) -> (
	print(str('Set %d blocks', length(story) ));
	global_history:dim += story;
	return('');
);

__undo(index, dim) -> (
	// iterate over the story backwards
	for(range(length(global_history:dim:index)-1, -1, -1),
		print(global_history:dim:index:_);
		set(global_history:dim:index:_:0, global_history:dim:index:_:1); // (position, block) pairs
	);
	// remove used story
	delete(global_history:dim, index);
);

go_back_stories(num) -> (
	//check for valid input
	if( type(num) != 'number' || num <= 0,
		print(format('rb Error: ', 'y Need a positive number of steps to go to'));
		return('')
	);

	dim = player() ~ 'dimension';

	index = length(global_history:dim)-num;
	if(index<0,
		print(format('rb Error: ', str('y You only have %d actions available to undo', length(global_history:dim) ) ));
		return('')
	);

	__undo(index, dim);
	print(str('Undid what you did %s actions ago', num ));
);

undo(num) -> (
	//check for valid input
	if( type(num) != 'number' || num <= 0,
		print(format('rb Error: ', 'y Need a positive number of steps to undo'));
		return('')
	);

	dim = player() ~ 'dimension';

	index = length(global_history:dim)-num;
	if(index<0,
		print(format('rb Error: ', str('y You only have %d actions to undo available', length(global_history:dim) ) ));
		return('')
	);

	loop(num, __undo(length(global_history:dim)-1, dim) );
	print(str('Undid the last %d actions', num) );
);

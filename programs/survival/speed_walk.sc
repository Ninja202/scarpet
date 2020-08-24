// gives a player speed for fast travel when holding quartz

//keeps script loaded upon server start
__config() -> (m(
  l('stay_loaded','true')
));

__on_player_uses_item(player, item, hand) ->
(
	if (item:0 == 'quartz',
        modify(player, 'effect', 'speed', 5, 100, 'false', 'false')
    )
);
